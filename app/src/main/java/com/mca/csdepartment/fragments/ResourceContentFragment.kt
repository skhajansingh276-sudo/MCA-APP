package com.mca.csdepartment.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mca.csdepartment.MainActivity
import com.mca.csdepartment.R
import com.mca.csdepartment.adapters.DocumentAdapter
import com.mca.csdepartment.models.DocumentModel
import com.mca.csdepartment.network.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray

class ResourceContentFragment : Fragment() {

    private var courseId: String = "msc"
    private var semesterNum: Int = 1
    private var resourceType: String = "Resources"
    
    private val documentList = mutableListOf<DocumentModel>()
    private lateinit var adapter: DocumentAdapter

    companion object {
        private const val ARG_COURSE = "course_id"
        private const val ARG_SEMESTER = "semester_number"
        private const val ARG_RESOURCE = "resource_type"

        fun newInstance(courseId: String, semester: Int, resourceType: String): ResourceContentFragment {
            return ResourceContentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE, courseId)
                    putInt(ARG_SEMESTER, semester)
                    putString(ARG_RESOURCE, resourceType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseId = arguments?.getString(ARG_COURSE) ?: "msc"
        semesterNum = arguments?.getInt(ARG_SEMESTER, 1) ?: 1
        resourceType = arguments?.getString(ARG_RESOURCE) ?: "Resources"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resource_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.updateToolbarForSemester(resourceType)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewResources)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvPlaceholder = view.findViewById<TextView>(R.id.tvResourcePlaceholder)

        adapter = DocumentAdapter(documentList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val formattedType = resourceType.lowercase().replace(" ", "_")
        
        progressBar.visibility = View.VISIBLE
        tvPlaceholder.visibility = View.GONE
        recyclerView.visibility = View.GONE

        fetchLocalDocuments(formattedType, progressBar, tvPlaceholder, recyclerView)
    }

    private fun fetchLocalDocuments(formattedType: String, progressBar: ProgressBar, tvPlaceholder: TextView, recyclerView: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            var jsonString: String? = null

            // 1. Try to fetch from Vercel Server
            try {
                val client = HttpClient.instance
                val request = Request.Builder()
                    .url(HttpClient.DOCUMENTS_JSON_URL)
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        jsonString = responseData
                        // Save in Cache (SharedPreferences)
                        context?.getSharedPreferences("mca_app_prefs", android.content.Context.MODE_PRIVATE)
                            ?.edit()
                            ?.putString("cached_documents_json", jsonString)
                            ?.apply()
                        Log.d("ResourceContent", "JSON database successfully synced from server!")
                    }
                }
            } catch (e: Exception) {
                Log.e("ResourceContent", "Network sync failed, loading from cache: ${e.message}")
            }

            // 2. If network fetch failed, load from local Cache (SharedPreferences)
            if (jsonString == null) {
                jsonString = context?.getSharedPreferences("mca_app_prefs", android.content.Context.MODE_PRIVATE)
                    ?.getString("cached_documents_json", null)
            }

            // 3. If Cache is also empty (first launch offline), fallback to internal assets documents.json
            if (jsonString == null) {
                try {
                    jsonString = context?.assets?.open("documents.json")?.bufferedReader()?.use { it.readText() }
                    Log.d("ResourceContent", "Assets documents.json loaded as fallback.")
                } catch (e: Exception) {
                    Log.e("ResourceContent", "Failed to load assets documents.json: ${e.message}")
                }
            }

            // 4. Parse JSON and update RecyclerView UI on Main Thread
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                progressBar.visibility = View.GONE

                if (jsonString != null) {
                    try {
                        val jsonArray = JSONArray(jsonString)
                        documentList.clear()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val course = obj.optString("course", "")
                            val semester = obj.optInt("semester", 0)
                            val resType = obj.optString("resource_type", "")

                            // Check if it matches course, semester, and resource type
                            if (course.equals(courseId, ignoreCase = true) &&
                                semester == semesterNum &&
                                resType.equals(formattedType, ignoreCase = true)) {
                                
                                val title = obj.optString("title", "Document")
                                val fileUrl = obj.optString("file_url", "")
                                documentList.add(DocumentModel(title, fileUrl, 0L))
                            }
                        }

                        if (documentList.isEmpty()) {
                            tvPlaceholder.visibility = View.VISIBLE
                            tvPlaceholder.text = "No documents found yet in $resourceType."
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        tvPlaceholder.visibility = View.VISIBLE
                        tvPlaceholder.text = "Error reading data: ${e.message}"
                    }
                } else {
                    tvPlaceholder.visibility = View.VISIBLE
                    tvPlaceholder.text = "Failed to load offline database."
                }
            }
        }
    }
}

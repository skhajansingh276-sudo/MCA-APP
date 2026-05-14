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
        private const val SUPABASE_URL = "https://uaagouglqhukybbxrvin.supabase.co"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVhYWdvdWdscWh1a3liYnhydmluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ2ODk5NjMsImV4cCI6MjA5MDI2NTk2M30.sL0tlLziCuUCB3OB62RrQHShz-QWKkatQWg7YLH_8Z4"

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

        fetchFromSupabase(formattedType, progressBar, tvPlaceholder, recyclerView)
    }

    private fun fetchFromSupabase(formattedType: String, progressBar: ProgressBar, tvPlaceholder: TextView, recyclerView: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use singleton OkHttp client for connection reuse → much faster
                val client = HttpClient.instance
                // Fetch using select query for specific course, semester, and resource type
                // Only select needed fields for faster response
                val url = "$SUPABASE_URL/rest/v1/documents?select=title,file_url&course=eq.$courseId&semester=eq.$semesterNum&resource_type=eq.$formattedType&order=created_at.desc"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && responseData != null) {
                        try {
                            val jsonArray = JSONArray(responseData)
                            documentList.clear()

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val title = obj.optString("title", "Document")
                                val fileUrl = obj.optString("file_url", "")
                                documentList.add(DocumentModel(title, fileUrl, 0L))
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
                            tvPlaceholder.text = "Error reading data formatting: ${e.message}"
                        }
                    } else {
                        tvPlaceholder.visibility = View.VISIBLE
                        tvPlaceholder.text = "Failed to load database. Code: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    progressBar.visibility = View.GONE
                    tvPlaceholder.visibility = View.VISIBLE
                    tvPlaceholder.text = "Network Error: ${e.message}"
                }
            }
        }
    }
}

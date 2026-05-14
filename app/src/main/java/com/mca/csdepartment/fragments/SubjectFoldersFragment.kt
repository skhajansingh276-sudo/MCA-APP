package com.mca.csdepartment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mca.csdepartment.MainActivity
import com.mca.csdepartment.R

/**
 * A reusable fragment that shows subject folders for any course/semester/resource type.
 * When a folder is clicked, it navigates to ResourceContentFragment with the specific resource type.
 * This can be used for Assignments, PYQs Bank, Sessional Test, or any future resource with sub-folders.
 */
class SubjectFoldersFragment : Fragment() {

    private var courseId: String = "mca"
    private var semesterNum: Int = 1
    private var resourceType: String = "Assignments" // Display name
    private var resourcePrefix: String = "Assignment" // DB prefix

    companion object {
        private const val ARG_COURSE = "course_id"
        private const val ARG_SEMESTER = "semester_number"
        private const val ARG_RESOURCE_TYPE = "resource_type"
        private const val ARG_RESOURCE_PREFIX = "resource_prefix"

        /**
         * Create a new instance of SubjectFoldersFragment.
         * @param courseId The course ID (e.g., "mca", "msc")
         * @param semester The semester number
         * @param resourceType Display name like "Assignments", "PYQs Bank"
         * @param resourcePrefix DB prefix like "Assignment", "Pyq", "Sessional"
         */
        fun newInstance(courseId: String, semester: Int, resourceType: String, resourcePrefix: String): SubjectFoldersFragment {
            return SubjectFoldersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE, courseId)
                    putInt(ARG_SEMESTER, semester)
                    putString(ARG_RESOURCE_TYPE, resourceType)
                    putString(ARG_RESOURCE_PREFIX, resourcePrefix)
                }
            }
        }
    }

    // Data class for folder info
    data class FolderItem(val displayName: String, val dbKey: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseId = arguments?.getString(ARG_COURSE) ?: "mca"
        semesterNum = arguments?.getInt(ARG_SEMESTER, 1) ?: 1
        resourceType = arguments?.getString(ARG_RESOURCE_TYPE) ?: "Assignments"
        resourcePrefix = arguments?.getString(ARG_RESOURCE_PREFIX) ?: "Assignment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_folders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.updateToolbarForSemester(resourceType)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFolders)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val folders = getSubjectFolders()
        recyclerView.adapter = FolderAdapter(folders) { folder ->
            // Navigate to ResourceContentFragment with the specific resource type
            (activity as? MainActivity)?.navigateToFragment(
                ResourceContentFragment.newInstance(courseId, semesterNum, "${resourcePrefix}_${folder.dbKey}")
            )
        }
    }

    /**
     * Returns the list of subject folders based on course and semester.
     * Add new entries here when you need new folders for any course/semester.
     */
    private fun getSubjectFolders(): List<FolderItem> {
        if (courseId == "mca") {
            return when (semesterNum) {
                1 -> listOf(
                    FolderItem("Computer Graphics", "Computer_Graphics"),
                    FolderItem("Oops in Java", "Oops_Java"),
                    FolderItem("Digital Design and Computer Architecture", "Digital_Design_Arch"),
                    FolderItem("Compiler Design", "Compiler_Design"),
                    FolderItem("Advanced Data Structure Using C++/Java", "Advanced_Data_Structure")
                )
                4 -> listOf(
                    FolderItem("Advance Software Engineering", "Advance_Software_Engineering"),
                    FolderItem("IoT & Sensor Networks", "IoT_Sensor_Networks"),
                    FolderItem("Web Development Using .NET Framework", "Web_Development_Using_DotNet"),
                    FolderItem("Cyber Security & Blockchain Technology", "Cyber_Security_Blockchain"),
                    FolderItem("Neural Networks & Deep Learning", "Neural_Networks_Deep_Learning")
                )
                else -> emptyList()
            }
        }
        return emptyList()
    }

    // ── RecyclerView Adapter ──
    inner class FolderAdapter(
        private val folders: List<FolderItem>,
        private val onClick: (FolderItem) -> Unit
    ) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

        inner class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvFolderName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder_card, parent, false)
            return FolderViewHolder(view)
        }

        override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
            val folder = folders[position]
            holder.tvName.text = folder.displayName
            holder.itemView.setOnClickListener { onClick(folder) }
        }

        override fun getItemCount() = folders.size
    }
}

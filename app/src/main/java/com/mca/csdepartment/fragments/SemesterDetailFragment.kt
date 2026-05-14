package com.mca.csdepartment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mca.csdepartment.MainActivity
import com.mca.csdepartment.R
import com.mca.csdepartment.databinding.FragmentSemesterDetailBinding

class SemesterDetailFragment : Fragment() {

    private var _binding: FragmentSemesterDetailBinding? = null
    private val binding get() = _binding!!

    private var semesterNumber: Int = 1
    private var courseId: String = "msc"
    private var isPracticalDropdownOpen = false

    companion object {
        private const val ARG_SEMESTER = "semester_number"
        private const val ARG_COURSE = "course_id"

        fun newInstance(courseId: String, semester: Int): SemesterDetailFragment {
            return SemesterDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE, courseId)
                    putInt(ARG_SEMESTER, semester)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        semesterNumber = arguments?.getInt(ARG_SEMESTER, 1) ?: 1
        courseId = arguments?.getString(ARG_COURSE) ?: "msc"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemesterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update toolbar title
        (activity as? MainActivity)?.updateToolbarForSemester("Semester $semesterNumber")

        // Set Syllabus Label dynamically
        binding.tvSyllabusLabel.text = "Semester $semesterNumber Syllabus"

        val navAction = { resourceType: String ->
            (activity as? MainActivity)?.navigateToFragment(
                ResourceContentFragment.newInstance(courseId, semesterNumber, resourceType)
            )
        }

        // Helper to navigate to SubjectFoldersFragment
        val openFolders = { displayName: String, prefix: String ->
            (activity as? MainActivity)?.navigateToFragment(
                SubjectFoldersFragment.newInstance(courseId, semesterNumber, displayName, prefix)
            )
        }

        // Practical Files dropdown toggle -> now routes to separate screen
        binding.itemPracticalFiles.setOnClickListener {
            if (courseId == "mca" && semesterNumber == 1) {
                togglePracticalDropdown()
            } else {
                navAction("Practical Files")
            }
        }

        binding.subItemBridgeCourse.setOnClickListener { navAction("Bridge Course") }
        binding.subItemLabRecords.setOnClickListener { navAction("Practical Files") }

        binding.itemLectureNotes.setOnClickListener { navAction("Lecture Notes") }

        // ── Assignments: Navigate to SubjectFoldersFragment if semesters have subject folders ──
        binding.itemAssignments.setOnClickListener {
            if (courseId == "mca" && (semesterNumber == 1 || semesterNumber == 4)) {
                openFolders("Assignments", "Assignment")
            } else {
                navAction("Assignments")
            }
        }

        // ── Sessional Test: Navigate to SubjectFoldersFragment for Sem 4 ──
        binding.itemSessionalTest.setOnClickListener {
            if (courseId == "mca" && semesterNumber == 4) {
                openFolders("Sessional Test", "Sessional")
            } else {
                navAction("Sessional Test")
            }
        }

        // ── PYQs Bank: Navigate to SubjectFoldersFragment for Sem 4 ──
        binding.itemPyqsBank.setOnClickListener {
            if (courseId == "mca" && semesterNumber == 4) {
                openFolders("PYQs Bank", "Pyq")
            } else {
                navAction("PYQs Bank")
            }
        }

        binding.itemSyllabus.setOnClickListener { navAction("Syllabus") }
    }

    private fun togglePracticalDropdown() {
        isPracticalDropdownOpen = !isPracticalDropdownOpen
        if (isPracticalDropdownOpen) {
            binding.layoutPracticalDropdown.visibility = View.VISIBLE
            binding.ivDropdownArrow.animate().rotation(90f).setDuration(200).start()
        } else {
            binding.layoutPracticalDropdown.visibility = View.GONE
            binding.ivDropdownArrow.animate().rotation(0f).setDuration(200).start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.restoreToolbar()
        _binding = null
    }
}

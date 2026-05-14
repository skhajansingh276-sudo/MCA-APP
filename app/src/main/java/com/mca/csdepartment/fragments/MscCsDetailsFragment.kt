package com.mca.csdepartment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mca.csdepartment.MainActivity
import com.mca.csdepartment.R
import com.mca.csdepartment.databinding.FragmentMscCsDetailsBinding

class MscCsDetailsFragment : Fragment() {
    private var _binding: FragmentMscCsDetailsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(courseId: String): MscCsDetailsFragment {
            val fragment = MscCsDetailsFragment()
            val args = Bundle()
            args.putString(ARG_COURSE_ID, courseId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMscCsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val courseId = arguments?.getString(ARG_COURSE_ID) ?: "msc"

        when (courseId) {
            "msc" -> {
                binding.tvCourseHeroTitle.text = "MSc (C.S.)"
                binding.tvCourseHeroDesc.text = "Master of Science in Computer Science. Advancing the frontiers of computational theory and architectural systems."
            }
            "mca" -> {
                binding.tvCourseHeroTitle.text = "MCA"
                binding.tvCourseHeroDesc.text = "Masters in Computer Applications. Focuses on application development, software engineering, and core computing concepts."
            }
            "dsml" -> {
                binding.tvCourseHeroTitle.text = "MSc (DSML)"
                binding.tvCourseHeroDesc.text = "Master of Science in Data Science and Machine Learning. Equipping students with advanced analytics and AI skills."
            }
        }

        // Course Syllabus click
        binding.btnCourseSyllabus.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(
                ResourceContentFragment.newInstance(courseId, 0, "Syllabus")
            )
        }

        // Semester 1 click — navigate to Semester Detail
        binding.cardSemester1.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(
                SemesterDetailFragment.newInstance(courseId, 1)
            )
        }

        // Semester 2 click
        binding.cardSemester2.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(
                SemesterDetailFragment.newInstance(courseId, 2)
            )
        }

        // Semester 3 click
        binding.cardSemester3.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(
                SemesterDetailFragment.newInstance(courseId, 3)
            )
        }

        // Semester 4 click
        binding.cardSemester4.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(
                SemesterDetailFragment.newInstance(courseId, 4)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

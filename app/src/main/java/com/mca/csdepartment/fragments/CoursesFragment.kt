package com.mca.csdepartment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mca.csdepartment.databinding.FragmentCoursesBinding

class CoursesFragment : Fragment() {
    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val navFunction = { courseId: String ->
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.slide_out_right
                )
                .replace(com.mca.csdepartment.R.id.fragment_container, MscCsDetailsFragment.newInstance(courseId))
                .addToBackStack(null)
                .commit()
        }

        binding.cardMscCs.setOnClickListener { navFunction("msc") }
        binding.cardMca.setOnClickListener { navFunction("mca") }
        binding.cardMscDsml.setOnClickListener { navFunction("dsml") }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

package com.mca.csdepartment.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mca.csdepartment.databinding.FragmentContactBinding

class ContactFragment : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivDeveloperProfile.setOnClickListener {
            val intent = Intent(requireContext(), com.mca.csdepartment.ProfileViewerActivity::class.java)
            intent.putExtra("EXTRA_NAME", "Khajan Singh")
            // Pass the local resource URI for the profile picture
            intent.putExtra("EXTRA_URL", "android.resource://${requireContext().packageName}/drawable/developer_profile")
            startActivity(intent)
        }

        binding.cardWhatsApp.setOnClickListener {
            openWhatsApp("919467960914")
        }
    }

    private fun openWhatsApp(number: String) {
        try {
            val url = "https://api.whatsapp.com/send?phone=$number"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

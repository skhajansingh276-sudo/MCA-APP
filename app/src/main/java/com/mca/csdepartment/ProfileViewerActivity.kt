package com.mca.csdepartment

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProfileViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_viewer)

        val imageUrl = intent.getStringExtra("EXTRA_URL")
        val name = intent.getStringExtra("EXTRA_NAME") ?: "Profile Photo"

        val imageView = findViewById<ImageView>(R.id.ivFullProfileImage)
        val btnClose = findViewById<ImageButton>(R.id.btnCloseProfile)
        val tvName = findViewById<TextView>(R.id.tvProfileName)

        tvName.text = name

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}

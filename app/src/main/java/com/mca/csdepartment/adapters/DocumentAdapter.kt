package com.mca.csdepartment.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mca.csdepartment.R
import com.mca.csdepartment.models.DocumentModel

class DocumentAdapter(private val documentList: List<DocumentModel>) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvDocumentTitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDocumentStatus)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivDocumentIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document_card, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val doc = documentList[position]
        holder.tvTitle.text = doc.title

        val lowerUrl = doc.fileUrl.lowercase()
        val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(doc.fileUrl).lowercase()

        when {
            lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".webp") -> {
                holder.tvStatus.text = "View Image"
                Glide.with(holder.itemView.context)
                    .load(doc.fileUrl)
                    .placeholder(R.drawable.bg_resource_icon)
                    .error(R.drawable.ic_resources)
                    .centerCrop()
                    .into(holder.ivIcon)
            }
            lowerUrl.endsWith(".doc") || lowerUrl.endsWith(".docx") -> {
                holder.tvStatus.text = "View Word Document"
                holder.ivIcon.setImageResource(R.drawable.ic_lecture_notes) // Temporary placeholder
                holder.ivIcon.setPadding(24, 24, 24, 24)
            }
            else -> {
                holder.tvStatus.text = "View PDF Document"
                holder.ivIcon.setImageResource(R.drawable.ic_syllabus) // Temporary placeholder
                holder.ivIcon.setPadding(24, 24, 24, 24)
            }
        }

        // Click to open file in appropriate viewer
        holder.itemView.setOnClickListener {
            val lowerCaseUrl = doc.fileUrl.lowercase()
            val isImage = lowerCaseUrl.endsWith(".jpg") || lowerCaseUrl.endsWith(".jpeg") || 
                          lowerCaseUrl.endsWith(".png") || lowerCaseUrl.endsWith(".gif") || 
                          lowerCaseUrl.endsWith(".webp")

            if (isImage) {
                val intent = Intent(it.context, com.mca.csdepartment.ProfileViewerActivity::class.java)
                intent.putExtra("EXTRA_NAME", doc.title)
                intent.putExtra("EXTRA_URL", doc.fileUrl)
                it.context.startActivity(intent)
            } else {
                val intent = Intent(it.context, com.mca.csdepartment.PdfViewerActivity::class.java)
                intent.putExtra("EXTRA_TITLE", doc.title)
                intent.putExtra("EXTRA_URL", doc.fileUrl)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return documentList.size
    }
}

package com.mca.csdepartment.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import com.mca.csdepartment.R

class CarouselAdapter(private val images: List<String>) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivCarouselImage)
        val bgImageView: ImageView = view.findViewById(R.id.ivCarouselBg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carousel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]

        // Load Blurred Background
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            .into(holder.bgImageView)

        // Load Actual Image with fixed sizing to prevent layout jumps
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.mdu_bg)
            .error(R.drawable.mdu_bg)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}
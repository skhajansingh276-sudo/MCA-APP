package com.mca.csdepartment.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mca.csdepartment.R
import com.mca.csdepartment.adapters.CarouselAdapter
import com.mca.csdepartment.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null

    private val carouselImages = listOf(
        "https://mdu.ac.in/UpFiles/UpImageFiles/2025/Aug/5_08-21-2025_13-59-52_MDU%20RANKING.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2026/Feb/5_02-04-2026_10-55-03_Student%20Portal%20Service%20Banner.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2019/Jan/Cultural%20Activities.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2019/Jan/Student%20Activity%20Centre.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2023/Apr/5_04-11-2023_13-25-03_library%20wallpaper.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2018/Oct/COE%20Building%20final.jpg",
        "https://mdu.ac.in/UpFiles/UpImageFiles/2019/Jan/MD%20University%20Secretariat.JPG"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Carousel
        val adapter = CarouselAdapter(carouselImages)
        binding.viewPagerCarousel.adapter = adapter
        
        // Hide side scroll/scrollbar
        binding.viewPagerCarousel.overScrollMode = View.OVER_SCROLL_NEVER
        val child = binding.viewPagerCarousel.getChildAt(0)
        if (child is RecyclerView) {
            child.overScrollMode = View.OVER_SCROLL_NEVER
            child.isHorizontalScrollBarEnabled = false
        }

        // Setup Manual Navigation Buttons
        binding.btnPrev.setOnClickListener {
            val currentItem = binding.viewPagerCarousel.currentItem
            val prevItem = if (currentItem == 0) carouselImages.size - 1 else currentItem - 1
            binding.viewPagerCarousel.setCurrentItem(prevItem, true)
            resetAutoSwipe()
        }

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPagerCarousel.currentItem
            val nextItem = (currentItem + 1) % carouselImages.size
            binding.viewPagerCarousel.setCurrentItem(nextItem, true)
            resetAutoSwipe()
        }

        // Start Auto-Swipe (3 seconds)
        startAutoSwipe()

        // Animate hero text in
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade_in)
        binding.tvUniversityName.startAnimation(slideUp)
        binding.tvEstablished.startAnimation(slideUp)
        binding.cardBadge1.startAnimation(slideUp)
        binding.cardBadge2.startAnimation(slideUp)

        // Animate content cards
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_up)
        binding.cardAbout.startAnimation(fadeIn)
    }

    private fun startAutoSwipe() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    val currentItem = binding.viewPagerCarousel.currentItem
                    val nextItem = (currentItem + 1) % carouselImages.size
                    binding.viewPagerCarousel.setCurrentItem(nextItem, true)
                }
            }
        }, 3000, 3000)
    }

    private fun resetAutoSwipe() {
        timer?.cancel()
        startAutoSwipe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }

    class ZoomOutPageTransformer : ViewPager2.PageTransformer {
        private val MIN_SCALE = 0.85f
        private val MIN_ALPHA = 0.5f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }
}

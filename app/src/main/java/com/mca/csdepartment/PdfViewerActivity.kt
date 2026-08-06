package com.mca.csdepartment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

import android.view.WindowManager
import java.security.MessageDigest

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback

class PdfViewerActivity : AppCompatActivity() {

    private var documentUrl: String = ""
    private var documentTitle: String = ""
    private var courseId: String = ""
    private var semesterNum: Int = 1

    private lateinit var pdfView: PDFView
    private lateinit var imageView: android.widget.ImageView
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load and show interstitial ad on open
        loadAndShowInterstitial()
        
        // ── Security: Prevent Screenshots & Screen Recording ──
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        
        setContentView(R.layout.activity_pdf_viewer)

        documentTitle = intent.getStringExtra("EXTRA_TITLE") ?: "Document Viewer"
        documentUrl = intent.getStringExtra("EXTRA_URL") ?: ""
        documentUrl = convertGoogleDriveUrl(documentUrl)
        courseId = intent.getStringExtra("EXTRA_COURSE") ?: ""
        semesterNum = intent.getIntExtra("EXTRA_SEMESTER", 1)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.pdfToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = documentTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        pdfView = findViewById(R.id.pdfView)
        imageView = findViewById(R.id.pdfImageView)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)

        if (documentUrl.isNotEmpty()) {
            val lowerUrl = documentUrl.lowercase()
            if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".webp")) {
                loadAsImage()
            } else {
                checkCacheAndLoad()
            }
        } else {
            loadingContainer.visibility = View.GONE
            Toast.makeText(this, "No document URL provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAsImage() {
        pdfView.visibility = View.GONE
        imageView.visibility = View.VISIBLE
        tvLoadingText.text = "Loading image..."

        com.bumptech.glide.Glide.with(this)
            .load(documentUrl)
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>, isFirstResource: Boolean): Boolean {
                    loadingContainer.visibility = View.GONE
                    Toast.makeText(this@PdfViewerActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return false
                }
                override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                    loadingContainer.visibility = View.GONE
                    return false
                }
            })
            .into(imageView)
    }

    private fun checkCacheAndLoad() {
        val hash = md5(documentUrl)
        val cacheFile = File(cacheDir, "pdf_$hash.pdf")

        if (cacheFile.exists() && cacheFile.length() > 0) {
            // File already in cache, load instantly!
            renderPdf(cacheFile)
        } else {
            loadAsPdf(cacheFile)
        }
    }

    private fun loadAsPdf(cacheFile: File) {
        imageView.visibility = View.GONE
        tvLoadingText.text = "Fetching document..."
        
        val request = Request.Builder().url(documentUrl).build()
        com.mca.csdepartment.network.HttpClient.instance.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingContainer.visibility = View.GONE
                    Toast.makeText(this@PdfViewerActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        loadingContainer.visibility = View.GONE
                        Toast.makeText(this@PdfViewerActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    // Stream body directly to file for speed and memory efficiency
                    val body = response.body ?: throw IOException("Empty response body")
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(cacheFile)
                    
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    runOnUiThread {
                        renderPdf(cacheFile)
                    }
                } catch (e: Exception) {
                    if (cacheFile.exists()) cacheFile.delete()
                    runOnUiThread {
                        loadingContainer.visibility = View.GONE
                        Toast.makeText(this@PdfViewerActivity, "Failed to download", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun renderPdf(file: File) {
        loadingContainer.visibility = View.GONE
        pdfView.visibility = View.VISIBLE
        
        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .password(null)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .spacing(10)
            .autoSpacing(true)
            .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.WIDTH)
            .pageSnap(true)
            .pageFling(true)
            .nightMode(false) // Can be toggled if needed
            .load()
    }

    private fun md5(s: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(s.toByteArray())
        val messageDigest = digest.digest()
        val hexString = StringBuilder()
        for (aMessageDigest in messageDigest) {
            var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
            while (h.length < 2) h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        // We keep the cache files for faster future loading
        // They are in cacheDir which Android can clean up if storage is low.
    }

    private fun loadAndShowInterstitial() {
        val adRequest = AdRequest.Builder().build()
        // Using Live interstitial ad unit ID
        InterstitialAd.load(this, "ca-app-pub-2060768890902501/2439327626", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            mInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            mInterstitialAd = null
                        }
                    }
                    if (!isFinishing && !isDestroyed) {
                        mInterstitialAd?.show(this@PdfViewerActivity)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }

    private fun convertGoogleDriveUrl(url: String): String {
        if (url.contains("drive.google.com/file/d/")) {
            val parts = url.split("drive.google.com/file/d/")
            if (parts.size > 1) {
                val idPart = parts[1].split("/")[0]
                return "https://drive.google.com/uc?export=download&id=$idPart"
            }
        }
        return url
    }
}

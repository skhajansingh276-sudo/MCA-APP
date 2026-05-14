package com.mca.csdepartment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.net.URLEncoder

class PdfViewerActivity : AppCompatActivity() {

    private var documentUrl: String = ""
    private var documentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        documentTitle = intent.getStringExtra("EXTRA_TITLE") ?: "Document Viewer"
        documentUrl = intent.getStringExtra("EXTRA_URL") ?: ""

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.pdfToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = documentTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val webView = findViewById<WebView>(R.id.pdfWebView)
        val imageView = findViewById<android.widget.ImageView>(R.id.pdfImageView)
        val loadingContainer = findViewById<LinearLayout>(R.id.loadingContainer)
        val tvLoadingText = findViewById<TextView>(R.id.tvLoadingText)

        // Bottom bar actions
        findViewById<LinearLayout>(R.id.btnDownload).setOnClickListener { downloadDocument() }
        findViewById<LinearLayout>(R.id.btnShare).setOnClickListener { shareDocument() }
        findViewById<LinearLayout>(R.id.btnOpenBrowser).setOnClickListener { openInBrowser() }

        // Configure WebView for maximum performance
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.setSupportMultipleWindows(false)
        settings.blockNetworkImage = false
        settings.loadsImagesAutomatically = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadingContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                tvLoadingText.text = "Failed to load document"
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // Let WebView handle all URLs
            }
        }

        webView.webChromeClient = WebChromeClient()

        if (documentUrl.isNotEmpty()) {
            val lowerUrl = documentUrl.lowercase()
            if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".webp")) {
                // ── Image Viewer ──
                webView.visibility = View.GONE
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
            } else {
                // ── PDF/Doc Viewer using Google Docs Viewer ──
                imageView.visibility = View.GONE
                tvLoadingText.text = "Loading document..."
                val encodedUrl = URLEncoder.encode(documentUrl, "UTF-8")
                val docsUrl = "https://docs.google.com/gview?url=$encodedUrl&embedded=true"
                webView.loadUrl(docsUrl)
            }
        } else {
            loadingContainer.visibility = View.GONE
            Toast.makeText(this, "No document URL provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadDocument() {
        if (documentUrl.isEmpty()) return
        
        val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(documentUrl) ?: "pdf"
        val fileName = "${documentTitle.replace(" ", "_")}.${if (extension.isNotEmpty()) extension else "pdf"}"

        try {
            val request = android.app.DownloadManager.Request(Uri.parse(documentUrl))
                .setTitle("Downloading $documentTitle")
                .setDescription("Saving document to your Downloads folder")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "⬇ Download Started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareDocument() {
        if (documentUrl.isEmpty()) return
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, documentTitle)
                putExtra(Intent.EXTRA_TEXT, "Check out this document: $documentTitle\n$documentUrl")
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInBrowser() {
        if (documentUrl.isEmpty()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl)))
        } catch (e: Exception) {
            Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
        }
    }
}

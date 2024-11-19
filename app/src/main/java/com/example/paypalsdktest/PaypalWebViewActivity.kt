package com.example.paypalsdktest

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.paypalsdktest.MainActivity.Companion.returnUrl
import com.example.paypalsdktest.databinding.ActivityPaypalWebViewBinding

class PaypalWebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaypalWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaypalWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val checkoutUrl = intent.getStringExtra("url") ?: ""
        setupWebView(checkoutUrl)
    }

    private fun setupWebView(url: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("com.example.paypalsdktest://")) {
                    handleRedirect(url)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // Handle page load completion if needed
            }
        }

        binding.webView.loadUrl(url)
    }

    private fun handleRedirect(url: String) {
        when {
            url.startsWith(returnUrl) -> {
                val resultIntent = Intent().apply {
                    putExtra("isSuccess", true)
                }
                setResult(RESULT_OK, resultIntent)
            }
            url.startsWith(returnUrl) -> {
                val resultIntent = Intent().apply {
                    putExtra("isSuccess", false)
                }
                setResult(RESULT_OK, resultIntent)
            }
        }
        finish()
    }

}
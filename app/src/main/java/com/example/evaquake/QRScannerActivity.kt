package com.example.evaquake

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QRScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner) // This layout file needs to be created

        val textView = findViewById<TextView>(R.id.qr_scanner_text)
        textView.text = "QR Scanner Functionality Coming Soon!"
    }
}

package com.mingwei.imagecolorpicker

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import com.mingwei.imagecolorpickerview.ImageColorPickerView

class MainActivity : AppCompatActivity() {

    private val listener = object : ImageColorPickerView.SelectColorListener {
        override fun onSelectColor(color: Color) {
            Log.d("${this::class.java}", color.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageColorPickerView>(R.id.color_picker)
            .addSelectColorListener(listener)

        val button = findViewById<Button>(R.id.select_image_button)
        button.setOnClickListener {
            openGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> {
                val uriString = data?.dataString
                val uri = Uri.parse(uriString)

                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    val src = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(src)
                }

                findViewById<ImageColorPickerView>(R.id.color_picker)
                    .setImageBitmap(bitmap)

                findViewById<ImageColorPickerView>(R.id.color_picker)
                    .visibility = View.VISIBLE
            }
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            startActivityForResult(it, 0)
        }
    }
}
package com.mingwei.imagecolorpicker

import android.content.Intent
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
import android.widget.SeekBar
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

        findViewById<SeekBar>(R.id.top_padding_seekbar)
            .setOnSeekBarChangeListener(topPaddingListener)

        findViewById<SeekBar>(R.id.bottom_padding_seekbar)
            .setOnSeekBarChangeListener(bottomPaddingListener)

        findViewById<SeekBar>(R.id.left_padding_seekbar)
            .setOnSeekBarChangeListener(leftPaddingListener)

        findViewById<SeekBar>(R.id.right_padding_seekbar)
            .setOnSeekBarChangeListener(rightPaddingListener)

        findViewById<SeekBar>(R.id.selector_probe_size_seekbar)
            .setOnSeekBarChangeListener(probeSizeListener)

        findViewById<SeekBar>(R.id.selector_size_seekbar)
            .setOnSeekBarChangeListener(selectorSizeListener)

        findViewById<SeekBar>(R.id.selector_offset_x)
            .setOnSeekBarChangeListener(offsetXListener)

        findViewById<SeekBar>(R.id.selector_offset_y)
            .setOnSeekBarChangeListener(offsetYListener)

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

    private val topPaddingListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            val padding = p1
            val view = findViewById<ImageColorPickerView>(R.id.color_picker)

            view.setPadding(view.paddingLeft, padding, view.paddingRight, view.paddingBottom)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val bottomPaddingListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            val padding = p1
            val view = findViewById<ImageColorPickerView>(R.id.color_picker)

            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, padding)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val leftPaddingListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            val padding = p1
            val view = findViewById<ImageColorPickerView>(R.id.color_picker)

            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val rightPaddingListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            val padding = p1
            val view = findViewById<ImageColorPickerView>(R.id.color_picker)

            view.setPadding(view.paddingLeft, view.paddingTop, padding, view.paddingBottom)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val probeSizeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .selectorProbeRadius = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val selectorSizeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .selectorRadius = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val offsetXListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .selectorOffsetX = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val offsetYListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .selectorOffsetY = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            startActivityForResult(it, 0)
        }
    }
}
package com.mingwei.imagecolorpicker

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.mingwei.imagecolorpickerview.ImageColorPickerView

class MainActivity : AppCompatActivity() {

    private val listener = object : ImageColorPickerView.PickColorListener {
        override fun onColorPicked(@ColorInt color: Int) {
            Log.d("${this::class.java}", color.toString())
        }

        override fun onColorUpdated(oldColor: Int?, newColor: Int) {
            Log.d("${this::class.java}", "$oldColor updated to $newColor")
        }

        override fun onPickStarted(color: Int) {
            Log.d("${this::class.java}", "Color Selection started with $color")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<ImageColorPickerView>(R.id.color_picker)
            .setImage(R.drawable.ic_android_black_24dp)

        findViewById<ImageColorPickerView>(R.id.color_picker)
            .setPickColorListener(listener)

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
                    .setImage(bitmap)
            }
        }
    }

    private val probeSizeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .pickerProbeRadius = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val selectorSizeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .pickerRadius = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val offsetXListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .pickerOffsetX = p1
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    private val offsetYListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            findViewById<ImageColorPickerView>(R.id.color_picker)
                .pickerOffsetY = p1
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
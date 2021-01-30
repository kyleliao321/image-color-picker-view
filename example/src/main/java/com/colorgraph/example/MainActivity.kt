package com.colorgraph.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.colorgraph.utils.imagecolorpickerview.ImageColorPickerView
import com.colorgraph.utils.imagecolorpickerview.pooling.PoolingFunction
import com.mingwei.example.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "MainActivity"
        const val OPEN_GALLERY_TAG = 0x00
    }

    private var mIndex = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_GALLERY_TAG -> {
                val uriString = data?.dataString
                val uri = Uri.parse(uriString)

                findViewById<ImageColorPickerView>(R.id.color_picker)
                    .setImage(uri)

                findViewById<Button>(R.id.select_image_button)
                    .visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageColorPickerView>(R.id.color_picker)
            .setPoolingFunc(PoolingFunction.BRIGHTEST_POOLING)

        findViewById<ImageColorPickerView>(R.id.color_picker)
            .setPickColorListener(pickColorListener)

        findViewById<Button>(R.id.select_image_button)
            .setOnClickListener { openGallery() }

        findViewById<ImageView>(R.id.picked_color_1)
            .setOnClickListener(palette0Listener)
        findViewById<ImageView>(R.id.picked_color_2)
            .setOnClickListener(palette1Listener)
        findViewById<ImageView>(R.id.picked_color_3)
            .setOnClickListener(palette2Listener)
        findViewById<ImageView>(R.id.picked_color_4)
            .setOnClickListener(palette3Listener)
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            startActivityForResult(it, OPEN_GALLERY_TAG)
        }
    }

    private val pickColorListener = object : ImageColorPickerView.PickColorListener {
        override fun onPickStarted(color: Int) {
            val targetView = when (mIndex) {
                0 -> findViewById<AppCompatImageView>(R.id.picked_color_1)
                1 -> findViewById<AppCompatImageView>(R.id.picked_color_2)
                2 -> findViewById<AppCompatImageView>(R.id.picked_color_3)
                3 -> findViewById<AppCompatImageView>(R.id.picked_color_4)
                else -> throw IllegalStateException()
            }

            targetView.setBackgroundColor(color)
        }

        override fun onColorUpdated(oldColor: Int?, newColor: Int) {
            val targetView = when (mIndex) {
                0 -> findViewById<AppCompatImageView>(R.id.picked_color_1)
                1 -> findViewById<AppCompatImageView>(R.id.picked_color_2)
                2 -> findViewById<AppCompatImageView>(R.id.picked_color_3)
                3 -> findViewById<AppCompatImageView>(R.id.picked_color_4)
                else -> throw IllegalStateException()
            }

            targetView.setBackgroundColor(newColor)
        }

        override fun onColorPicked(color: Int) {
            val targetView = when (mIndex) {
                0 -> findViewById<AppCompatImageView>(R.id.picked_color_1)
                1 -> findViewById<AppCompatImageView>(R.id.picked_color_2)
                2 -> findViewById<AppCompatImageView>(R.id.picked_color_3)
                3 -> findViewById<AppCompatImageView>(R.id.picked_color_4)
                else -> throw IllegalStateException()
            }

            targetView.setBackgroundColor(color)
        }
    }

    private val palette0Listener = View.OnClickListener {
        mIndex = 0
    }

    private val palette1Listener = View.OnClickListener {
        mIndex = 1
    }

    private val palette2Listener = View.OnClickListener {
        mIndex = 2
    }

    private val palette3Listener = View.OnClickListener {
        mIndex = 3
    }
}
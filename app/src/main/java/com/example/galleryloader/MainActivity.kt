package com.example.galleryloader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.carlol.android.galleryloader.GalleryLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLoadFromGalleryView.setOnClickListener {
            GalleryLoader.showGalleryChooser(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val bitmap = GalleryLoader.processGalleryChooserResult(this, requestCode, resultCode, data)

        sampleImageView.setImageBitmap(bitmap)
    }

}

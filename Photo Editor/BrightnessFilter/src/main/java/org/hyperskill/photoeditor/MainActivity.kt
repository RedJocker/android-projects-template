package org.hyperskill.photoeditor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.set
import com.google.android.material.slider.Slider


class MainActivity : AppCompatActivity() {

    private val currentImage: ImageView by lazy {
        findViewById<ImageView>(R.id.ivPhoto)
    }

    private val galleryButton: Button by lazy {
        findViewById<Button>(R.id.btnGallery)
    }

    private val brightnessSlider: Slider by lazy {
        findViewById<Slider>(R.id.slBrightness)
//            .also {
//                it.stepSize = 0.2f       // should produce ""slBrightness" should have proper stepSize attribute expected:<10.0> but was:<0.2>"
//                it.valueFrom = 0f       // should produce ""slBrightness" should have proper valueFrom attribute expected:<-250.0> but was:<0.0>"
//                it.valueTo = 500f      // should produce ""slBrightness" should have proper valueTo attribute expected:<250.0> but was:<500.0>"
//                it.value = 100f       // should produce ""slBrightness" should have proper initial value expected:<0.0> but was:<100.0>"
//            }
    }

    private val registerForActivityResult =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                currentImage.setImageURI(uri)
                currentImageDrawable = currentImage.drawable as BitmapDrawable?
            }
        }

    private var currentImageDrawable: BitmapDrawable? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()

        //do not change this line
        currentImage.setImageBitmap(createBitmap())         // commenting this line should produce "is "ivPhoto" not empty and no crash occurs while swiping slider?"
        currentImageDrawable = currentImage.drawable as BitmapDrawable?
    }


    private fun setListener() {
        galleryButton.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            registerForActivityResult.launch(intent)    // commenting this line should produce "No intent was found by tests. Have you launched an intent?"
        }

        brightnessSlider.addOnChangeListener { slider, value, fromUser ->
            val bitmap = currentImageDrawable?.bitmap ?: return@addOnChangeListener
            val copy = bitmap.copy(Bitmap.Config.RGB_565, true)
            val height = bitmap.height
            val width = bitmap.width
            val intValue = value.toInt() // + 200  // should produce "Wrong values after brightness applied. expected <(220, 250, 255)> actual <(255, 255, 255)>"

            for(y in 0 until height) {
                for(x in 0 until width) {
                    val color = bitmap.getPixel(x, y)
                    val oldRed = Color.red(color)
                    val oldBlue = Color.blue(color)
                    val oldGreen = Color.green(color)
                    val red = when {
                        oldRed + intValue > 255 -> 255
                        oldRed + intValue < 0 -> 0
                        else -> oldRed + intValue
                    }
                    val blue = when {
                        oldBlue + intValue > 255 -> 255
                        oldBlue + intValue < 0 -> 0
                        else -> oldBlue + intValue
                    }
                    val green = when {
                        oldGreen + intValue > 255 -> 255
                        oldGreen + intValue < 0 -> 0
                        else -> oldGreen + intValue
                    }
                    copy[x, y] = Color.rgb(red, green, blue)
                }
            }
            currentImage.setImageBitmap(copy)
//            currentImage.setImageBitmap(null)    // should produce "Image was null after slBrightness triggered"
//            currentImageDrawable = currentImage.drawable as BitmapDrawable?  // should produce "Wrong values after brightness applied. expected: <(__, __, __)> actual: <(__, __, __)>"
        }


    }

    // do not change this function
    fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x+y) % 100 + 120

                pixels[index] = Color.rgb(R,G,B)
//                pixels[index] = Color.rgb(R + 20,G,B)  // should produce "is defaultBitmap set correctly? expected:<(130, 140, 150)> but was:<(110, 140, 150)>"

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}
package org.hyperskill.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.hyperskill.photoeditor.TestUtils.assertColorsValues
import org.hyperskill.photoeditor.TestUtils.findViewByString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import kotlin.AssertionError

//version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage5UnitTest {

    private val messageNullInitialImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageNullAfterFilters = "Image was null after filters been applied"
    private val messageWrongValues = "Wrong values after filters been applied."
    private val marginError = 3

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    private val activity = activityController.setup().get()


    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto") }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery") }
    private val btnSave by lazy { activity.findViewByString<Button>("btnSave") }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness") }
    private val slContrast by lazy { activity.findViewByString<Slider>("slContrast") }
    private val slSaturation by lazy { activity.findViewByString<Slider>("slSaturation") }
    private val slGamma by lazy { activity.findViewByString<Slider>("slGamma") }
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    @Test
    fun testShouldCheckSliderExist() {
        val message2 = "\"slSaturation\" should have proper stepSize attribute"
        assertEquals(message2,  10f, slSaturation.stepSize)

        val message3 = "\"slSaturation\" should have proper valueFrom attribute"
        assertEquals(message3,  -250f, slSaturation.valueFrom)

        val message4 = "\"slSaturation\" should have proper valueTo attribute"
        assertEquals(message4,  250f, slSaturation.valueTo)

        val message5 = "\"slSaturation\" should have proper initial value"
        assertEquals(message5,  0f, slSaturation.value)
    }

    @Test
    fun testShouldCheckGammaSliderExist() {
        val message2 = "\"slGamma\" should have proper stepSize attribute"
        assertEquals(message2,  0.2f, slGamma.stepSize)

        val message3 = "\"slGamma\" should have proper valueFrom attribute"
        assertEquals(message3,  0.2f, slGamma.valueFrom)

        val message4 = "\"slGamma\" should have proper valueTo attribute"
        assertEquals(message4,  4f, slGamma.valueTo)

        val message5 = "\"slGamma\" should have proper initial value"
        assertEquals(message5,  1f, slGamma.value)
    }

    @Test
    fun testShouldCheckSliderNotCrashingByDefault() {
        slSaturation.value += slSaturation.stepSize
        slGamma.value += slGamma.stepSize
        shadowLooper.runToEndOfTasks()
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullInitialImage)
        val expectedRgb = Triple(46, 212, 249)


        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 4
        slContrast.value += slContrast.stepSize
        slSaturation.value += slSaturation.stepSize * 10
        slSaturation.value += slSaturation.stepSize * 5
        slGamma.value -= slGamma.stepSize * 2

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullInitialImage)
        val expectedRgb = Triple(70, 121, 182)

        slGamma.value += slGamma.stepSize * 5
        slSaturation.value += slSaturation.stepSize * 5
        slBrightness.value += slBrightness.stepSize
        slContrast.value -= slContrast.stepSize
        slBrightness.value += slBrightness.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 90, 80)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    private fun singleColor(source: Bitmap, x:Int = 70, y:Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }

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

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}
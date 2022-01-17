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
class Stage6UnitTest {

    private val messageNullInitialImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageNullAfterFilters = "Image was null after filters been applied"
    private val messageWrongValues = "Wrong values after filters been applied."
    private val messageSynchronousCode = "Are your filters being applied asynchronously.?"
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
    fun testShouldCheckHighBrightnessValue() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)

        slBrightness.value += 120
        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)


        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(230, 255, 255)
        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeContrastValue() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)

        slContrast.value += 100
        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(85, 154, 177)
        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeSaturationValue() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)

        slSaturation.value += 80
        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(88, 146, 165)
        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeGammaValue() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)


        slGamma.value += 4 * slGamma.stepSize

        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(56, 86, 98)
        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)


        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 4
        slContrast.value += slContrast.stepSize
        slSaturation.value += slSaturation.stepSize * 10
        slSaturation.value += slSaturation.stepSize * 5
        slGamma.value -= slGamma.stepSize * 2

        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(36, 208, 246)
        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb = singleColor(actualImage, 70, 60)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageNullInitialImage
        )
        val initialRgb = singleColor(initialImage, 70, 60)


        slGamma.value += slGamma.stepSize * 5
        slSaturation.value += slSaturation.stepSize * 5
        slBrightness.value += slBrightness.stepSize
        slContrast.value -= slContrast.stepSize
        slBrightness.value += slBrightness.stepSize

        Thread.sleep(200)
        val actualImage0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actualRgb0 = singleColor(actualImage0, 70, 60)

        shadowLooper.runToEndOfTasks()
        assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val expectedRgb = Triple(71, 122, 186)
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
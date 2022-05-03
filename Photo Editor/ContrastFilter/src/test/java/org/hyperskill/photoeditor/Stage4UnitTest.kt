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

// version 1.0
@RunWith(RobolectricTestRunner::class)
class Stage4UnitTest {

    private val messageNullAfterFilters = "Image was null after filters been applied"
    private val messageWrongValues = "Wrong values after filters been applied."
    private val marginError = 3

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    private val activity = activityController.setup().get()
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(this::testShouldCheckImageIsSetToDefaultBitmap)
    }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery")
        .also { testShouldCheckButton(it, "GALLERY", "btnGallery") }
    }
    private val btnSave by lazy { activity.findViewByString<Button>("btnSave")
        .also { testShouldCheckButton(it, "SAVE", "btnSave") }
    }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness")
        .also { testShouldCheckSlider(it, "slBrightness") }
    }
    private val slContrast by lazy { activity.findViewByString<Slider>("slContrast")
        .also { testShouldCheckSlider(it, "slContrast") }
    }


    private fun testShouldCheckImageIsSetToDefaultBitmap(ivPhoto: ImageView) {
        val messageInitialImageNull = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
        val messageWrongInitialImage = "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
        val actualBitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageInitialImageNull
        )
        assertTrue(messageWrongInitialImage, 200 == actualBitmap.width)
        assertTrue(messageWrongInitialImage, 100 == actualBitmap.height)
        val expectedRgb = Triple(110, 140, 150)
        assertTrue(messageWrongInitialImage, expectedRgb == singleColor(actualBitmap))
    }

    private fun testShouldCheckButton(btn: Button, expectedInitialText: String, btnName: String) {
        assertEquals("Wrong text for $btnName",
            expectedInitialText.toUpperCase(), btn.text.toString().toUpperCase()
        )
    }

    private fun testShouldCheckSlider(
        slBrightness: Slider, sliderName: String, expectedStepSize: Float = 10f ,
        expectedValueFrom: Float = -250f, expectedValueTo: Float = 250f, expectedValue: Float = 0f) {

        val message1 = "\"$sliderName\" should have proper stepSize attribute"
        assertEquals(message1, expectedStepSize, slBrightness.stepSize)

        val message2 = "\"$sliderName\" should have proper valueFrom attribute"
        assertEquals(message2, expectedValueFrom, slBrightness.valueFrom)

        val message3 = "\"$sliderName\" should have proper valueTo attribute"
        assertEquals(message3, expectedValueTo, slBrightness.valueTo)

        val message4 = "\"$sliderName\" should have proper initial value"
        assertEquals(message4, expectedValue, slBrightness.value)
    }

    @Test
    fun testShouldCheckImageView() {
        ivPhoto // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderBrightness() {
        slBrightness // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderContrast() {
        slContrast // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonGallery() {
        btnGallery // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonSave() {
        btnSave  // initializes variable and perform initialization assertions
    }


    @Test
    fun testShouldCheckSliderContrastNotCrashingByDefault() {
        ivPhoto
        slContrast.value += slContrast.stepSize
        slContrast.value -= slContrast.stepSize
        shadowLooper.runToEndOfTasks()
        (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: AssertionError(messageNullAfterFilters)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        slBrightness
        slContrast
        ivPhoto

        val expected = Triple(141, 210, 255)

        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 9
        slContrast.value += slContrast.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actual = singleColor(actualImage, 90, 80)
        assertColorsValues("$messageWrongValues For x=90, y=80", expected, actual, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        slBrightness
        slContrast
        ivPhoto
        val expected = Triple(149, 177, 205)

        slBrightness.value += slBrightness.stepSize
        slBrightness.value += slBrightness.stepSize
        slContrast.value -= slContrast.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actual = singleColor(actualImage, 90, 80)
        assertColorsValues("$messageWrongValues For x=90, y=80", expected, actual, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEditWiderSample() {
        slBrightness
        slContrast
        ivPhoto

        val sample = listOf(
            10 to 40,
            30 to 99,
            150 to 32,
            99 to 20,
            10 to 50,
            190 to 0,
            5 to 60,
            70 to 90,
            0 to 0,
            30 to 80
        )
        val expected = listOf(
            Triple(0, 118, 232),
            Triple(3, 253, 184),
            Triple(49, 100, 255),
            Triple(161, 72, 161),
            Triple(0, 141, 255),
            Triple(141, 26, 255),
            Triple(0, 164, 255),
            Triple(95, 232, 255),
            Triple(0, 26, 118),
            Triple(3, 210, 141),
        )

        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 9
        slContrast.value += slContrast.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)

        for(i in sample.indices) {
            val point = sample[i]
            val actual = singleColor(actualImage, point.first, point.second)
            assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                expected[i], actual, marginError
            )
        }
    }

    @Test
    fun testOrderOfSlidersEventsShouldNotMatter() {
        slBrightness
        slContrast
        ivPhoto
        val expected = Triple(141, 210, 255)
        val wrongExpected = Triple(140, 170, 200)  //happens if brightness slider ignores contrast value

        slContrast.value += slContrast.stepSize * 9
        slContrast.value += slContrast.stepSize
        slBrightness.value += slBrightness.stepSize

        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        val actual = singleColor(actualImage, 90, 80)
        val messageWrongOrder =
            if(actual == wrongExpected) " Order of slider events should not matter."
            else ""
        assertColorsValues("$messageWrongValues $messageWrongOrder For x=90, y=80", expected, actual, marginError)
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
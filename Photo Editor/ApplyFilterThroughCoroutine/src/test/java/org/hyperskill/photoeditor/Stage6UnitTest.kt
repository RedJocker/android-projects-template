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
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import kotlin.AssertionError

//version 0.4
@RunWith(RobolectricTestRunner::class)
class Stage6UnitTest {

    private val messageNullAfterFilters = "Image was null after filters been applied"
    private val messageWrongValues = "Wrong values after filters been applied."
    private val messageSynchronousCode = "Are your filters being applied asynchronously?"
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
    private val slSaturation by lazy { activity.findViewByString<Slider>("slSaturation")
        .also { testShouldCheckSlider(it, "slSaturation") }
    }
    private val slGamma by lazy { activity.findViewByString<Slider>("slGamma")
        .also { testShouldCheckSlider(it, "slGamma",
            0.2f, 0.2f, 4f, 1f)
        }
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
        Assert.assertEquals("Wrong text for $btnName",
            expectedInitialText.toUpperCase(), btn.text.toString().toUpperCase()
        )
    }

    private fun testShouldCheckSlider(
        slBrightness: Slider, sliderName: String, expectedStepSize: Float = 10f ,
        expectedValueFrom: Float = -250f, expectedValueTo: Float = 250f, expectedValue: Float = 0f) {

        val message1 = "\"$sliderName\" should have proper stepSize attribute"
        Assert.assertEquals(message1, expectedStepSize, slBrightness.stepSize)

        val message2 = "\"$sliderName\" should have proper valueFrom attribute"
        Assert.assertEquals(message2, expectedValueFrom, slBrightness.valueFrom)

        val message3 = "\"$sliderName\" should have proper valueTo attribute"
        Assert.assertEquals(message3, expectedValueTo, slBrightness.valueTo)

        val message4 = "\"$sliderName\" should have proper initial value"
        Assert.assertEquals(message4, expectedValue, slBrightness.value)
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
    fun testShouldCheckSliderSaturation() {
        slSaturation // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderGamma() {
        slGamma // initializes variable and perform initialization assertions
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
    fun testShouldCheckHighBrightnessValue() {
        slBrightness
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeContrastValue() {
        slContrast
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeSaturationValue() {
        slSaturation
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckSomeGammaValue() {
        slGamma
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit2() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
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
        assertColorsValues("$messageWrongValues For x=90, y=80", expectedRgb, actualRgb, marginError)
    }

    @Test
    fun testShouldCheckDefaultBitmapEditWiderSample() {
        slBrightness
        slContrast
        slSaturation
        slGamma
        ivPhoto

        val sample = listOf(
            15 to 41,
            30 to 98,
            150 to 42,
            97 to 20,
            11 to 50,
            191 to 4,
            5 to 62,
            73 to 90,
            1 to 0,
            39 to 80
        )
        val expectedBefore = listOf(
            Triple(55, 121, 176),
            Triple(70, 178, 148),
            Triple(90, 122, 212),
            Triple(137, 100, 137),
            Triple(51, 130, 181),
            Triple(131, 84, 215),
            Triple(45, 142, 187),
            Triple(113, 170, 183),
            Triple(41, 80, 121),
            Triple(79, 160, 139)
        )

        val expectedAfter = listOf(
            Triple(0, 180, 255),
            Triple(0, 255, 242),
            Triple(0, 99, 255),
            Triple(223, 0, 223),
            Triple(0, 206, 255),
            Triple(143, 0, 255),
            Triple(0, 240, 255),
            Triple(0, 255, 255),
            Triple(0, 110, 255),
            Triple(0, 255, 225),
        )

        slBrightness.value += slBrightness.stepSize
        slContrast.value += slContrast.stepSize * 4
        slContrast.value += slContrast.stepSize
        slSaturation.value += slSaturation.stepSize * 10
        slSaturation.value += slSaturation.stepSize * 5
        slGamma.value -= slGamma.stepSize * 2

        val actualBeforeImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        shadowLooper.runToEndOfTasks()
        for(i in sample.indices) {
            val point = sample[i]
            val actual = singleColor(actualBeforeImage, point.first, point.second)
            assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                expectedBefore[i], actual, marginError
            )
        }

        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualAfterImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterFilters)
        for(i in sample.indices) {
            val point = sample[i]
            val actual = singleColor(actualAfterImage, point.first, point.second)
            assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                expectedAfter[i], actual, marginError
            )
        }

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
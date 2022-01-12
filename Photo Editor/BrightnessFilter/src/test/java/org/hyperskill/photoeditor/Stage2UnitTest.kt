package org.hyperskill.photoeditor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import kotlin.AssertionError
import kotlin.math.max
import kotlin.math.min

// version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest {

    private val messageNullImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"
    private val messageWrongValues = "Wrong values after brightness applied."


    private val activityController: ActivityController<MainActivity> = Robolectric.buildActivity(MainActivity::class.java)
    private val activity: MainActivity = activityController.setup().get()

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto") }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery") }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness") }
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    private val marginError = 1

    @Test
    fun testShouldCheckSliderExist() {
        val message2 = "\"slBrightness\" should have proper stepSize attribute"
        assertEquals(message2, 10f, slBrightness.stepSize)

        val message3 = "\"slBrightness\" should have proper valueFrom attribute"
        assertEquals(message3, -250f, slBrightness.valueFrom,)

        val message4 = "\"slBrightness\" should have proper valueTo attribute"
        assertEquals(message4, 250f, slBrightness.valueTo,)

        val message5 = "\"slBrightness\" should have proper initial value"
        assertEquals(message5, 0f, slBrightness.value)
    }

    @Test
    fun testShouldCheckSliderNotCrashingByDefault() {
        val message = "is \"ivPhoto\" not empty and no crash occurs while swiping slider?"

        slBrightness.value += slBrightness.stepSize
        slBrightness.value -= slBrightness.stepSize
        shadowLooper.runToEndOfTasks()
        val bitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
        assertNotNull(message, bitmap)
    }

    @Test
    fun testShouldCheckImageIsSetToDefaultBitmap() {
        val message = "is defaultBitmap set correctly?"

        val actualBitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullImage)
        val expectedBitmap = createBitmap()
        assertEquals(message, singleColor(actualBitmap), singleColor(expectedBitmap))
        assertEquals(message, expectedBitmap.width, actualBitmap.width)
        assertEquals(message, expectedBitmap.height, actualBitmap.height,)
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullImage)
        val (initialRed, initialGreen, initialBlue) = singleColor(initialImage)

        val expectedRgb1 =
            Triple(initialRed + 110, initialGreen + 110, initialBlue + 105)
        val expectedRgb2 =
            Triple(initialRed - 110, initialGreen - 120, initialBlue - 120)

        slBrightness.value += slBrightness.stepSize * 5
        slBrightness.value += slBrightness.stepSize * 6
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb1 = singleColor(actualImage1)
        assertColorsValues(messageWrongValues, expectedRgb1, actualRgb1, marginError)

        slBrightness.value -= slBrightness.stepSize * 10
        slBrightness.value -= slBrightness.stepSize * 13
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage2 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb2 = singleColor(actualImage2)
        assertColorsValues(messageWrongValues, expectedRgb2, actualRgb2, marginError)
    }


    @Test
    fun testShouldCheckNewBitmapEdit() {
        val messageNullAfterLoading = "Image was null after loading from gallery"

        btnGallery.performClick()
        shadowLooper.runToEndOfTasks()

        val activityResult: Intent = createGalleryPickActivityResultStub(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult()?.intent
            ?: throw AssertionError(messageIntentNotFound)

        shadowActivity.receiveResult(
            intent,
            Activity.RESULT_OK,
            activityResult
        )
        shadowLooper.runToEndOfTasks()

        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterLoading)
        val (initialRed, initialGreen, initialBlue) = singleColor(initialImage)
        val expectedRgb = Triple(initialRed + 50, initialGreen + 50, initialBlue + 50)


        slBrightness.value += slBrightness.stepSize * 3
        slBrightness.value += slBrightness.stepSize * 2
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()


        val actualImage = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb = singleColor(actualImage, 80, 90)
        assertColorsValues(messageWrongValues, expectedRgb, actualRgb, marginError)
    }

    private fun singleColor(source: Bitmap, x: Int = 70, y: Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }

    private fun createBitmap(): Bitmap {
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
                B = (x + y) % 100 + 120

                pixels[index] = Color.rgb(R, G, B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }

    fun changeBrightness(colorValue: Int, filterValue: Int): Int {
        return max(min(colorValue + filterValue, 255), 0)
    }

    fun createGalleryPickActivityResultStub(activity: MainActivity): Intent {
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity, R.drawable.myexample)
        resultIntent.setData(uri)
        return resultIntent
    }

    fun getUriToDrawable(context: Context, drawableId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }
}
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest {

    private val messageNullImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"
    private val messageWrongValuesFormat = "Wrong values after brightness applied. expected <(%d, %d, %d)> actual <(%d, %d, %d)>"


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
        val message2 = "\"slider\" should have proper stepSize attribute"
        assertEquals(message2, slBrightness.stepSize, 10f)

        val message3 = "\"slider\" should have proper valueFrom attribute"
        assertEquals(message3, slBrightness.valueFrom, -250f)

        val message4 = "\"slider\" should have proper valueTo attribute"
        assertEquals(message4, slBrightness.valueTo, 250f)

        val message5 = "\"slider\" should have proper initial value"
        assertEquals(message5, slBrightness.value, 0f)
    }

    @Test
    fun testShouldCheckSliderNotCrashingByDefault() {
        val message = "is \"ivPhoto\" not empty and no crash occurs while swiping slider?"

        slBrightness.value += slBrightness.stepSize
        val bitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
        shadowLooper.runToEndOfTasks()
        assertNotNull(message, bitmap)
        slBrightness.value -= slBrightness.stepSize
    }

    @Test
    fun testShouldCheckImageIsSetToDefaultBitmap() {
        val message = "is defaultBitmap set correctly?"

        val bitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullImage)
        val bitmap2 = createBitmap()
        assertEquals(message, singleColor(bitmap), singleColor(bitmap2))
        assertEquals(message, bitmap.width, bitmap2.width)
    }


    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val img0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
            ?: throw AssertionError(messageNullImage)
        val (initialRed, initialGreen, initialBlue) = singleColor(img0)
        val (expectedRed1, expectedGreen1, expectedBlue1) = Triple(
            initialRed + 110, initialGreen + 110, initialBlue + 105
        )
        val (expectedRed2, expectedGreen2, expectedBlue2) = Triple(
            initialRed - 110, initialGreen - 120, initialBlue - 120
        )

        slBrightness.value += slBrightness.stepSize * 5
        slBrightness.value += slBrightness.stepSize * 6
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val img1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val (actualRed1, actualGreen1, actualBlue1) = singleColor(img1)
        val messageWrongValues1 = messageWrongValuesFormat.format(
            expectedRed1, expectedGreen1, expectedBlue1,
            actualRed1, actualGreen1, actualBlue1
        )

        assertTrue(messageWrongValues1, abs(expectedRed1 - actualRed1) <= marginError)
        assertTrue(messageWrongValues1, abs(expectedGreen1 - actualGreen1) <= marginError)
        assertTrue(messageWrongValues1, abs(expectedBlue1 - actualBlue1) <= marginError)

        slBrightness.value -= slBrightness.stepSize * 10
        slBrightness.value -= slBrightness.stepSize * 13
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val img3 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val (actualRed2, actualGreen2, actualBlue2) = singleColor(img3)
        val messageWrongValues2 = messageWrongValuesFormat.format(
            expectedRed2, expectedGreen2, expectedBlue2,
            actualRed2, actualGreen2, actualBlue2
        )

        assertTrue(messageWrongValues2, abs(expectedRed2 - actualRed2) <= marginError)
        assertTrue(messageWrongValues2, abs(expectedGreen2 - actualGreen2) <= marginError)
        assertTrue(messageWrongValues2, abs(expectedBlue2 - actualBlue2) <= marginError)
    }


    @Test
    fun testShouldCheckNewBitmapEdit() {
        val messageNullAfterLoading = "Image was null after loading from gallery"
        val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"

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

        val img0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
            ?: throw AssertionError(messageNullAfterLoading)
        val (initialRed, initialGreen, initialBlue) = singleColor(img0)


        slBrightness.value += slBrightness.stepSize * 3
        slBrightness.value += slBrightness.stepSize * 2
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()


        val img2 = (ivPhoto.drawable as BitmapDrawable).bitmap
            ?: throw AssertionError(messageNullAfterSlBrightness)
        val (actualRed, actualGreen, actualBlue) = singleColor(img2, 80, 90)
        val (expectedRed, expectedGreen, expectedBlue) =
            Triple(initialRed + 50, initialGreen + 50, initialBlue + 50)

        val messageWrongValues = messageWrongValuesFormat.format(
            expectedRed, expectedGreen, expectedBlue,
            actualRed, actualGreen, actualBlue
        )
        assertTrue(messageWrongValues, abs(expectedRed - actualRed) <= marginError)
        assertTrue(messageWrongValues, abs(expectedGreen - actualGreen) <= marginError)
        assertTrue(messageWrongValues, abs(expectedBlue - actualBlue) <= marginError)
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
        val bitmapOut = Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
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
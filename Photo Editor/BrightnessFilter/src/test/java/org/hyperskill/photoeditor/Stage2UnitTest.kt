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

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.AssertionError

// version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest {

    private val messageNullImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"

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
        val RGB0 = singleColor(img0)

        slBrightness.value += slBrightness.stepSize * 5
        slBrightness.value += slBrightness.stepSize * 6
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()


        val img2 = (ivPhoto.drawable as BitmapDrawable).bitmap
        val RGB2 = singleColor(img2)
        val message2 = "val0 ${RGB0} val2 ${RGB2}"

        assertTrue(message2, abs(RGB0.first + 110 - RGB2.first) <= marginError)
        assertTrue(message2, abs(RGB0.second + 110 - RGB2.second) <= marginError)
        assertTrue(message2, abs(RGB0.third + 105 - RGB2.third) <= marginError)


        slBrightness.value -= slBrightness.stepSize*10
        slBrightness.value -= slBrightness.stepSize*13
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()


        val img3 = (ivPhoto.drawable as BitmapDrawable).bitmap
        val RGB3 = singleColor(img3)
        val message3 = "val0 ${RGB0} val2 ${RGB3}"

        assertTrue(message3, abs(RGB0.first - 110 - RGB3.first) <= marginError)
        assertTrue(message3, abs(RGB0.second - 120 - RGB3.second) <= marginError)
        assertTrue(message3, abs(RGB0.third - 120 - RGB3.third) <= marginError)
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
        val RGB0 = singleColor(img0)


        slBrightness.value += slBrightness.stepSize * 3
        slBrightness.value += slBrightness.stepSize * 2
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()



        val img2 = (ivPhoto.drawable as BitmapDrawable).bitmap
            ?: throw AssertionError(messageNullAfterSlBrightness)
        val RGB2 = singleColor(img2, 80, 90)
        val message2 = "val0 ${RGB0} val2 ${RGB2}"

        assertTrue(message2, abs(RGB0.first+50-RGB2.first) <= marginError)
        assertTrue(message2, abs(RGB0.second+50-RGB2.second) <= marginError)
        assertTrue(message2, abs(RGB0.third+50-RGB2.third) <= marginError)
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
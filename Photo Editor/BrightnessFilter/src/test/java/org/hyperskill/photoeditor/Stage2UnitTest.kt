package org.hyperskill.photoeditor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider

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

import org.hyperskill.util.TestUtils.findViewByString
import org.hyperskill.util.TestUtils.extractPixelRgb
import org.hyperskill.util.TestUtils.testShouldCheckButton
import org.hyperskill.util.TestUtils.testShouldCheckImageIsSetToDefaultBitmap
import org.hyperskill.util.TestUtils.testShouldCheckSlider
import org.hyperskill.util.TestUtils.assertColorsValues

// version 0.5
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest {

    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"
    private val messageWrongValues = "Wrong values after brightness applied."

    private val activityController: ActivityController<MainActivity> = Robolectric.buildActivity(MainActivity::class.java)
    private val activity: MainActivity = activityController.setup().get()
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }
    private val marginError = 1

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(::testShouldCheckImageIsSetToDefaultBitmap)
    }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery")
        .also { testShouldCheckButton(it, "GALLERY", "btnGallery") }
    }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness")
        .also { testShouldCheckSlider(it, "slBrightness") }
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
    fun testShouldCheckButtonGallery() {
        btnGallery // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSliderNotCrashing() {
        ivPhoto // initializes variable and perform initialization assertions
        slBrightness.value += slBrightness.stepSize
        slBrightness.value -= slBrightness.stepSize
        shadowLooper.runToEndOfTasks()
        val bitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
        assertNotNull(messageNullAfterSlBrightness, bitmap)
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        slBrightness
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization
        val (initialRed, initialGreen, initialBlue) = extractPixelRgb(initialImage)

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
        val actualRgb1 = extractPixelRgb(actualImage1)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb1, actualRgb1, marginError)

        slBrightness.value -= slBrightness.stepSize * 10
        slBrightness.value -= slBrightness.stepSize * 13
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage2 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb2 = extractPixelRgb(actualImage2)
        assertColorsValues("$messageWrongValues For x=70, y=60", expectedRgb2, actualRgb2, marginError)
    }


    @Test
    fun testShouldCheckDefaultBitmapEditExhaustive() {
        slBrightness
        val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization

        slBrightness.value += slBrightness.stepSize * 15
        slBrightness.value -= slBrightness.stepSize * 4
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)

        for (x in 0 until initialImage.width) {
            for (y in 0 until initialImage.height) {
                val (initialRed, initialGreen, initialBlue) = extractPixelRgb(initialImage, x, y)
                val expectedRed = max(0, min(initialRed + 110, 255))
                val expectedGreen = max(0, min(initialGreen + 110, 255))
                val expectedBlue = max(0, min(initialBlue + 110, 255))
                val expectedRgb1 = Triple(expectedRed, expectedGreen, expectedBlue)
                val actualRgb1 = extractPixelRgb(actualImage1, x, y)
                assertColorsValues("$messageWrongValues For x=$x, y=$y", expectedRgb1, actualRgb1, marginError)
            }
        }
    }


    @Test
    fun testShouldCheckNewBitmapEdit() {
        ivPhoto // initializes variable and perform initialization assertions
        slBrightness

        btnGallery.performClick()
        shadowLooper.runToEndOfTasks()

        val activityStubResult: Intent = createGalleryPickActivityResultStub(activity)
        val actualIntent = shadowActivity.peekNextStartedActivityForResult()?.intent
            ?: throw AssertionError(messageIntentNotFound)

        val expectedIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        assertTrue(
            "Intent found was different from expected." +
                    " expected <$expectedIntent> actual <$actualIntent>",
            actualIntent.filterEquals(expectedIntent)
        )

        val messageNullAfterLoading = "Image was null after loading from gallery"
        shadowActivity.receiveResult(
            actualIntent,
            Activity.RESULT_OK,
            activityStubResult
        )
        shadowLooper.runToEndOfTasks()

        val initialImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(messageNullAfterLoading)
        val (initialRed, initialGreen, initialBlue) = extractPixelRgb(initialImage, 80, 90)
        val expectedRgb = Triple(initialRed + 50, initialGreen + 50, initialBlue + 50)

        slBrightness.value += slBrightness.stepSize * 3
        slBrightness.value += slBrightness.stepSize * 2
        shadowLooper.runToEndOfTasks()
        Thread.sleep(200)
        shadowLooper.runToEndOfTasks()

        val actualImage = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(messageNullAfterSlBrightness)
        val actualRgb = extractPixelRgb(actualImage, 80, 90)
        assertColorsValues("$messageWrongValues For x=80, y=90", expectedRgb, actualRgb, marginError)
    }

    private fun createGalleryPickActivityResultStub(activity: MainActivity): Intent {
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity, R.drawable.myexample)
        resultIntent.data = uri
        return resultIntent
    }

    private fun getUriToDrawable(context: Context, drawableId: Int): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.resources.getResourcePackageName(drawableId)
                    + '/' + context.resources.getResourceTypeName(drawableId)
                    + '/' + context.resources.getResourceEntryName(drawableId)
        )
    }
}
package org.hyperskill.photoeditor

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.junit.Assert.*
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import java.io.ByteArrayOutputStream
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
class Stage3UnitTest {
    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageWrongValues = "Wrong values after brightness applied."
    private val marginError = 1

    private val activityController: ActivityController<MainActivity> = Robolectric.buildActivity(MainActivity::class.java)
    private val activity: MainActivity = activityController.setup().get()
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(::testShouldCheckImageIsSetToDefaultBitmap)
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
    fun testShouldCheckButtonSave() {
        btnSave  // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckSomeNewBitmapIsCreated() {
        btnSave
        shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val bitmapExpected = (ivPhoto.drawable as BitmapDrawable).bitmap
        val expectedContent = ByteArrayOutputStream()
        bitmapExpected.compress(Bitmap.CompressFormat.JPEG, 100, expectedContent)

        val contentResolver = activity.contentResolver
        val actualContent = ByteArrayOutputStream()
        val shadowContentResolver = shadowOf(contentResolver)
        val uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/1").also{
            shadowLooper.runToEndOfTasks()
        } ?: throw AssertionError("Test failed to parse Uri")
        val messageError = "image loaded from $uri had wrong"

        shadowContentResolver.registerOutputStream(uri, actualContent)
        btnSave.performClick()
        shadowLooper.runToEndOfTasks()

        val bitmapActual = BitmapFactory.decodeByteArray(
            actualContent.toByteArray(), 0, actualContent.size()
        )

        assertEquals("$messageError width", bitmapExpected.width, bitmapActual.width)
        assertEquals("$messageError height", bitmapExpected.height, bitmapActual.height)
        assertArrayEquals("$messageError content", expectedContent.toByteArray(), actualContent.toByteArray())
    }

    @Test
    fun testShouldCheckPermission() {
        btnSave
        val messagePermissionRequired = "Have you asked permission to write?"
        btnSave.performClick()
        shadowLooper.runToEndOfTasks()
        val permissionRequest = shadowActivity.lastRequestedPermission ?: throw AssertionError(messagePermissionRequired)
        val hasRequestedPermission = permissionRequest.requestedPermissions.filter { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }.any()
        assert(hasRequestedPermission) { messagePermissionRequired }
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
        assertColorsValues(messageWrongValues, expectedRgb1, actualRgb1, marginError)

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
}
package org.hyperskill.photoeditor

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import org.hyperskill.photoeditor.TestUtils.findViewByString
import org.junit.Assert.*
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.abs

// version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage3UnitTest {
    private val messageNullInitialImage = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
    private val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
    private val messageWrongValuesFormat = "Wrong values after brightness applied. expected <(%d, %d, %d)> actual <(%d, %d, %d)>"
    private val marginError = 1

    private val activityController: ActivityController<MainActivity> = Robolectric.buildActivity(MainActivity::class.java)
    private val activity: MainActivity = activityController.setup().get()

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto") }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery") }
    private val btnSave by lazy { activity.findViewByString<Button>("btnSave") }
    private val slBrightness by lazy { activity.findViewByString<Slider>("slBrightness") }
    private val shadowActivity: ShadowActivity by lazy { Shadows.shadowOf(activity) }
    private val shadowLooper: ShadowLooper by lazy { Shadows.shadowOf(Looper.getMainLooper()) }

    @Test
    fun testShouldCheckSaveButtonExist() {
        assertEquals("Wrong text for btnSave",
            "SAVE", btnSave.text.toString().toUpperCase()
        )
    }

    @Test
    fun testShouldCheckSomeNewBitmapIsCreated() {
        shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val bitmapExpected = (ivPhoto.drawable as BitmapDrawable).bitmap
            ?: throw AssertionError(messageNullInitialImage)
        val contentResolver = activity.contentResolver
        val output = ByteArrayOutputStream()
        val shadowContentResolver = shadowOf(contentResolver)
        val uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/1").also{
            shadowLooper.runToEndOfTasks()
        } ?: throw AssertionError("Test failed to parse Uri")
        val message2 = uri.toString()
        shadowContentResolver.registerOutputStream(uri, output)
        btnSave.performClick()
        shadowContentResolver.registerInputStream(uri, ByteArrayInputStream(output.toByteArray()))
        shadowLooper.runToEndOfTasks()
        val bitmapActual = contentResolver.openInputStream(uri).use(BitmapFactory::decodeStream)
            ?: throw AssertionError("Test failed to decode bitmap")
        shadowLooper.runToEndOfTasks()
        assertEquals(message2, bitmapExpected.width, bitmapActual.width)
        assertEquals(message2, bitmapExpected.height, bitmapActual.height)
    }


    @Test
    fun testShouldCheckPermission() {
        val messagePermissionRequired = "Have you asked permission to write?"
        btnSave.performClick()
        shadowLooper.runToEndOfTasks()
        val permissionRequest = shadowActivity.lastRequestedPermission ?: throw AssertionError(messagePermissionRequired)
        val hasRequestedPermission = permissionRequest.requestedPermissions.filter { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }.any()
        assert(hasRequestedPermission) { messagePermissionRequired }
    }

    @Test
    fun testShouldCheckDefaultBitmapEdit() {
        val img0 = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
            ?: throw AssertionError(messageNullInitialImage)
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

    private fun singleColor(source: Bitmap, x: Int = 70, y: Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }

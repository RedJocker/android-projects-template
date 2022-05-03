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
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import org.hyperskill.photoeditor.TestUtils.findViewByString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowActivity
import org.robolectric.Shadows.shadowOf
import java.lang.NullPointerException
import kotlin.AssertionError

// version 1.0
@RunWith(RobolectricTestRunner::class)
class Stage1UnitTest {

    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    private val activity = activityController.setup().get()

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto")
        .also(this::testShouldCheckImageIsSetToDefaultBitmap)
    }
    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery")
        .also { testShouldCheckButton(it, "GALLERY", "btnGallery") }
    }
    private val shadowActivity: ShadowActivity by lazy { shadowOf(activity) }



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
            expectedInitialText.uppercase(), btn.text.toString().uppercase()
        )
    }

    @Test
    fun testShouldCheckImageView() {
        ivPhoto // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonGallery() {
        btnGallery // initializes variable and perform initialization assertions
    }

    @Test
    fun testShouldCheckButtonOpensGallery() {
        btnGallery.performClick()

        val expectedIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        val actualIntent = shadowActivity.nextStartedActivity
            ?: throw AssertionError(messageIntentNotFound)

        assertTrue(
            "Intent found was different from expected." +
                    " expected <$expectedIntent> actual <$actualIntent>",
            actualIntent.filterEquals(expectedIntent)
        )
    }

    @Test
    fun testShouldCheckButtonLoadsImage() {
        ivPhoto // initializes variable and perform initialization assertions
        btnGallery.performClick()
        val activityResult = createGalleryPickActivityResultStub(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult()?.intent
            ?: throw AssertionError(messageIntentNotFound)
        shadowActivity.receiveResult(
            intent, Activity.RESULT_OK, activityResult
        )
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        val messageNullAfterLoading = "Image was null after loading from gallery"
        assertNotNull(messageNullAfterLoading, ivPhoto.drawable)

        val actualDrawableId: Int = try {
            shadowOf(ivPhoto.drawable).createdFromResId   // shadowOf(ivPhoto.drawable) can throw NullPointer if .setImageBitmap(null)
        } catch (ex: NullPointerException) {
            throw AssertionError(messageNullAfterLoading)
        }

        assertEquals("Drawable loaded is different from expected.",
            R.drawable.myexample, actualDrawableId
        )
    }

    private fun createGalleryPickActivityResultStub(activity: MainActivity): Intent {
        val resultIntent = Intent()
        val uri = getUriToDrawable(activity,R.drawable.myexample)
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

    private fun singleColor(source: Bitmap, x: Int = 70, y: Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }
}
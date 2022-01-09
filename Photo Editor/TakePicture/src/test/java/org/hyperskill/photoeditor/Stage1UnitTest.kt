package org.hyperskill.photoeditor

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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

// version 0.2
@RunWith(RobolectricTestRunner::class)
class Stage1UnitTest {

    private val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"

    private val activityController = Robolectric.buildActivity(MainActivity::class.java)
    private val activity = activityController.setup().get()

    private val ivPhoto by lazy { activity.findViewByString<ImageView>("ivPhoto") }

    private val btnGallery by lazy { activity.findViewByString<Button>("btnGallery") }

    private val shadowActivity: ShadowActivity by lazy { shadowOf(activity) }

    @Test
    fun testShouldCheckImageViewExist() {
        ivPhoto
    }

    @Test
    fun testShouldCheckImageViewImageNotEmpty() {
        val drawable = (ivPhoto.drawable)
        val message2 = "is \"ivPhoto\" not empty?"

        assertNotNull(message2, drawable)
    }

    @Test
    fun testShouldCheckButtonExist() {
        btnGallery
    }

    @Test
    fun testShouldCheckButtonOpensGallery() {

        btnGallery.performClick()

        // The intent we expect to be launched when a user clicks on the button
        val expectedIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        // An Android "Activity" doesn't expose a way to find out about activities it launches
        // Robolectric's "ShadowActivity" keeps track of all launched activities and exposes this information
        // through the "getNextStartedActivity" method.
        val actualIntent = shadowActivity.nextStartedActivity
            ?: throw AssertionError(messageIntentNotFound)

        // Determine if two intents are the same for the purposes of intent resolution (filtering).
        // That is, if their action, data, type, class, and categories are the same. This does
        // not compare any extra data included in the intents
        assertTrue(
            "Intent found was different from expected." +
                    " expected <$expectedIntent> actual <$actualIntent>",
            actualIntent.filterEquals(expectedIntent)
        )

    }

    @Test
    fun testShouldCheckButtonLoadsImage() {

        btnGallery.performClick()
        val activityResult = createGalleryPickActivityResultStub(activity)
        val intent = shadowActivity.peekNextStartedActivityForResult()?.intent
            ?: throw AssertionError(messageIntentNotFound)
        shadowActivity.receiveResult(
            intent, Activity.RESULT_OK, activityResult
        )
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        val messageIvPhotoWasNull = "ivPhoto drawable was null"
        assertNotNull(messageIvPhotoWasNull, ivPhoto.drawable)

        val actualDrawableId: Int = try {
            shadowOf(ivPhoto.drawable).createdFromResId   // shadowOf(ivPhoto.drawable) can throw NullPointer if .setImageBitmap(null)
        } catch (ex: NullPointerException) {
            throw AssertionError(messageIvPhotoWasNull)
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
}
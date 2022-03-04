package org.hyperskill.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.slider.Slider
import org.junit.Assert
import kotlin.math.abs


object TestUtils {

    inline fun <reified T> Activity.findViewByString(idString: String): T {
        val id = this.resources.getIdentifier(idString, "id", this.packageName)
        val view: View? = this.findViewById(id)

        val idNotFoundMessage = "View with id \"$idString\" was not found"
        val wrongClassMessage = "View with id \"$idString\" is not from expected class. " +
                "Expected ${T::class.java.simpleName} found ${view?.javaClass?.simpleName}"

        Assert.assertNotNull(idNotFoundMessage, view)
        Assert.assertTrue(wrongClassMessage, view is T)

        return view as T
    }

    fun extractPixelRgb(source: Bitmap, x: Int = 70, y: Int = 60): Triple<Int, Int, Int> {
        val pixel = source.getPixel(x, y)

        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        return  Triple(red,green,blue)
    }

    fun assertColorsValues(message: String, expected: Triple<Int, Int, Int>, actual: Triple<Int, Int, Int>, marginError: Int) {
        val messageWrongValuesFormat = "%s expected: <(%d, %d, %d)> actual: <(%d, %d, %d)>"
        val (expectedRed, expectedGreen, expectedBlue) = expected
        val (actualRed, actualGreen, actualBlue) = actual

        val messageWrongValues = messageWrongValuesFormat.format( message,
            expectedRed, expectedGreen, expectedBlue,
            actualRed, actualGreen, actualBlue
        )

        Assert.assertTrue(messageWrongValues, abs(expectedRed - actualRed) <= marginError)
        Assert.assertTrue(messageWrongValues, abs(expectedGreen - actualGreen) <= marginError)
        Assert.assertTrue(messageWrongValues, abs(expectedBlue - actualBlue) <= marginError)
    }

    fun testShouldCheckImageIsSetToDefaultBitmap(ivPhoto: ImageView) {
        val messageInitialImageNull = "Initial image was null, it should be set with ___.setImageBitmap(createBitmap())"
        val messageWrongInitialImage = "Is defaultBitmap set correctly? It should be set with ___.setImageBitmap(createBitmap())"
        val actualBitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
            messageInitialImageNull
        )
        Assert.assertTrue(messageWrongInitialImage, 200 == actualBitmap.width)
        Assert.assertTrue(messageWrongInitialImage, 100 == actualBitmap.height)
        val expectedRgb = Triple(110, 140, 150)
        Assert.assertTrue(messageWrongInitialImage, expectedRgb == extractPixelRgb(actualBitmap))
    }

    fun testShouldCheckButton(btn: Button, expectedInitialText: String, btnName: String) {
        Assert.assertEquals(
            "Wrong text for $btnName",
            expectedInitialText.toUpperCase(), btn.text.toString().toUpperCase()
        )
    }

    fun testShouldCheckSlider(
        slider: Slider, sliderName: String, expectedStepSize: Float = 10f,
        expectedValueFrom: Float = -250f, expectedValueTo: Float = 250f, expectedValue: Float = 0f) {

        val message1 = "\"$sliderName\" should have proper stepSize attribute"
        Assert.assertEquals(message1, expectedStepSize, slider.stepSize)

        val message2 = "\"$sliderName\" should have proper valueFrom attribute"
        Assert.assertEquals(message2, expectedValueFrom, slider.valueFrom)

        val message3 = "\"$sliderName\" should have proper valueTo attribute"
        Assert.assertEquals(message3, expectedValueTo, slider.valueTo)

        val message4 = "\"$sliderName\" should have proper initial value"
        Assert.assertEquals(message4, expectedValue, slider.value)
    }
}

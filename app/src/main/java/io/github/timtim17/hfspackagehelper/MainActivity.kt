package io.github.timtim17.hfspackagehelper

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer


class MainActivity : AppCompatActivity() {

    private val messageRegex = Regex("Locker Bank: (.+) Stripe\r?\n?Pin: (\\d+)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        when {
            intent?.action == Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT).let {
                    if (it == null) {
                        handleError(R.string.errWeirdStart)
                    } else {
                        val match = messageRegex.find(it)
                        if (match == null) {
                            handleError(R.string.errInvalidMsg)
                        } else {
                            val (boxColor, barcode) = match.destructured
                            val colorBoxView = findViewById<FrameLayout>(R.id.box)
                            val boxColorConv = boxColor.replace(" ", "")
                            val colorId = resources.getIdentifier("colorBox$boxColorConv", "color", packageName)
                            val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                resources.getColor(colorId, theme) else resources.getColor(colorId)
                            colorBoxView.setBackgroundColor(color)
                            val colorText = findViewById<TextView>(R.id.boxName)
                            colorText.text = if (boxColor == "No") "No Color" else boxColor
                            colorText.setTextColor(resources.getColor(if (boxColor == "No") android.R.color.black else android.R.color.white))
                            val imageView = findViewById<ImageView>(R.id.barcode)
                            imageView.setImageBitmap(generateBarcode(barcode))
                            val barcodeText = findViewById<TextView>(R.id.barcodeText)
                            barcodeText.text = barcode
                        }
                    }
                }
            }
            else -> {
                handleError(R.string.errWeirdStart)
            }
        }
    }

    /**
     * Shows a toast to the user in the event of an error and closes the app.
     * @param errorTextRes The id of the string resource with the error message to show
     */
    private fun handleError(errorTextRes: Int) {
        Toast.makeText(this, resources.getText(errorTextRes), Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * Generates a Code-128 barcode of the given contents.
     * @param contents The text to encode in the barcode
     * @return The barcode as a Bitmap image
     */
    private fun generateBarcode(contents: String): Bitmap {
        val bitMatrixCode = Code128Writer().encode(contents, BarcodeFormat.CODE_128, 400, 300)
        val width = bitMatrixCode.width
        val height = bitMatrixCode.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrixCode.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}

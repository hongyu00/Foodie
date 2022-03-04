package my.com.foodie.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Blob
import my.com.foodie.R
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Usage: Crop and resize bitmap (upscale)
 * References: Mr.Liaw Practical
 */
fun Bitmap.crop(width: Int, height: Int): Bitmap {
    // Source width, height and ratio
    val sw = this.width
    val sh = this.height
    val sratio = 1.0 * sw / sh

    // Target offset (x, y), width, height and ratio
    val x: Int
    val y: Int
    val w: Int
    val h: Int
    val ratio = 1.0 * width / height

    if (ratio >= sratio) {
        // Retain width, calculate height
        w = sw
        h = (sw / ratio).toInt()
        x = 0
        y = (sh - h) / 2
    }
    else {
        // Retain height, calculate width
        w = (sh * ratio).toInt()
        h = sh
        x = (sw - w) / 2
        y = 0
    }

    return Bitmap
        .createBitmap(this, x, y, w, h) // Crop
        .scale(width, height) // Resize
}

/**
 * Usage: Convert from Bitmap to Blob
 * References: Mr.Liaw Practical
 */
@Suppress("DEPRECATION")
fun Bitmap.toBlob(): Blob {
    ByteArrayOutputStream().use {
        this.compress(Bitmap.CompressFormat.WEBP, 80, it)
        return Blob.fromBytes(it.toByteArray())
    }
}

/**
 * Usage: Convert from Blob to Bitmap
 * References: Mr.Liaw Practical
 */
fun Blob.toBitmap(): Bitmap? {
    val bytes = this.toBytes()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

/**
 * Usage: Crop to Blob
 * References: Mr.Liaw Practical
 */
fun ImageView.cropToBlob(width: Int, height: Int): Blob {
    return if (this.drawable == null)
        Blob.fromBytes(ByteArray(0))
    else
        this.drawable.toBitmap().crop(width, height).toBlob()
}

/**
 * Usage: Show an error dialog from fragment
 * References: Mr.Liaw Practical
 */
fun Fragment.errorDialog(text: String) {
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_error)
        .setTitle(R.string.error)
        .setMessage(text)
        .setPositiveButton(getString(R.string.dismiss), null)
        .show()
}

/**
 * Usage: Show an info dialog from fragment
 */
fun Fragment.successDialog(title:String, text: String, pv: () -> Unit = {}) {
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_info)
        .setTitle(title)
        .setMessage(text)
        .setPositiveButton(getString(R.string.ok)) { _, _ -> pv() }
        .show()
}

/**
 * Usage: Show a snackbar from fragment
 * References: Mr.Liaw Practical
 */
fun Fragment.snackbar(text: String) {
    Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
}

/**
 * Usage: Hide keyboard from fragment
 * References: Mr.Liaw Practical
 */
fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

/**
 * Usage: Show selection dialog and pass the text, first option function, second option function to carry
 */
fun Fragment.showPhotoSelection(text: String, cameraLauncher: () -> Unit, photoLauncher: () -> Unit) {
    val items: Array<CharSequence> = arrayOf<CharSequence>("Take a Photo", "Choose From Gallery")
    AlertDialog.Builder(context)
        .setTitle(text)
        .setIcon(R.drawable.ic_select_photo)
        .setSingleChoiceItems(items, 3) { d, n ->
            pickImage(n, cameraLauncher,  photoLauncher)
            d?.dismiss()
        }
        .setNegativeButton(getString(R.string.cancel), null).show()
}

/**
 * Usage: Get user selection and call respective function
 */
private fun pickImage(n: Int, cameraLauncher: () -> Unit, photoLauncher: () -> Unit) {
    if (n == 0) cameraLauncher()
    else photoLauncher()
}

/**
 * Usage: Generate new ID
 */
fun generateID(lastID: String): String {
    val idChar = lastID.takeWhile { it.isLetter() }
    val idNum  = lastID.takeLastWhile { !it.isLetter() }
    Log.d("check id", "$idChar.....$idNum")
    val fmt = DecimalFormat("0000000")
    val str = fmt.format(idNum.toInt() + 1)

    return idChar + str
}


package com.example.campusconnectapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    /**
     * Converts an image Uri into a compressed Base64 data string for cross-device sync via Firestore.
     * Scales image down to max 800px width/height and compresses to JPEG 70% quality (~30-80 KB).
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // Calculate scaled dimensions (max 800px)
            val maxDimension = 800
            val width = originalBitmap.width
            val height = originalBitmap.height

            val (newWidth, newHeight) = if (width > maxDimension || height > maxDimension) {
                if (width > height) {
                    maxDimension to (height * maxDimension / width)
                } else {
                    (width * maxDimension / height) to maxDimension
                }
            } else {
                width to height
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun base64ToByteArray(base64Str: String?): ByteArray? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val pureBase64 = if (base64Str.contains(",")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            Base64.decode(pureBase64, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getLoadableModel(modelStr: String?): Any? {
        if (modelStr.isNullOrBlank()) return null
        if (modelStr.startsWith("data:image") || modelStr.contains("base64") || (modelStr.length > 200 && !modelStr.startsWith("http"))) {
            return base64ToByteArray(modelStr) ?: modelStr
        }
        return modelStr
    }
}

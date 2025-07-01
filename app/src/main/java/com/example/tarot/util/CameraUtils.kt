package com.example.tarot.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtils {

    private const val TAG = "CameraUtils"

    fun createImageFile(context: Context): File {
        // Create an image file name with timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PALM_${timeStamp}_", ".jpg", storageDir)
    }

    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Clean up temporary palm reading image after processing
    fun cleanupImageFile(uri: Uri, context: Context) {
        try {
            val file = getFileFromUri(uri, context)
            if (file?.exists() == true) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Successfully deleted temporary palm image: ${file.name}")
                } else {
                    Log.w(TAG, "Failed to delete temporary palm image: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up palm image file", e)
        }
    }

    // Clean up all old palm reading images (cleanup on app start)
    fun cleanupOldPalmImages(context: Context) {
        try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            storageDir?.listFiles { file ->
                file.name.startsWith("PALM_") && file.name.endsWith(".jpg")
            }?.forEach { file ->
                // Delete images older than 1 hour (3600000 milliseconds)
                val oneHourAgo = System.currentTimeMillis() - 3600000
                if (file.lastModified() < oneHourAgo) {
                    val deleted = file.delete()
                    Log.d(TAG, "Cleaned up old palm image: ${file.name}, deleted: $deleted")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old palm images", e)
        }
    }

    private fun getFileFromUri(uri: Uri, context: Context): File? {
        return try {
            val path = uri.path
            if (path != null && path.contains("PALM_")) {
                File(path)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file from URI", e)
            null
        }
    }
}
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
            Log.d(TAG, "Attempting to cleanup image file from URI: $uri")
            val file = getFileFromUri(uri, context)
            if (file?.exists() == true) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Successfully deleted temporary palm image: ${file.name}")
                } else {
                    Log.w(TAG, "Failed to delete temporary palm image: ${file.name}")
                }
            } else {
                Log.w(TAG, "File not found for cleanup: ${file?.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up palm image file", e)
        }
    }

    // Clean up all old palm reading images (cleanup on app start)
    fun cleanupOldPalmImages(context: Context) {
        try {
            Log.d(TAG, "Starting cleanup of old palm images...")
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            if (storageDir == null) {
                Log.e(TAG, "Storage directory is null, cannot cleanup palm images")
                return
            }

            if (!storageDir.exists()) {
                Log.d(TAG, "Storage directory doesn't exist, no cleanup needed")
                return
            }

            Log.d(TAG, "Scanning directory: ${storageDir.absolutePath}")

            val palmFiles = storageDir.listFiles { file ->
                file.name.startsWith("PALM_") && file.name.endsWith(".jpg")
            }

            if (palmFiles.isNullOrEmpty()) {
                Log.d(TAG, "No palm image files found for cleanup")
                return
            }

            Log.d(TAG, "Found ${palmFiles.size} palm image files")

            val oneHourAgo = System.currentTimeMillis() - 3600000 // 1 hour
            var deletedCount = 0

            palmFiles.forEach { file ->
                val ageInMinutes = (System.currentTimeMillis() - file.lastModified()) / 60000
                Log.d(TAG, "Checking file: ${file.name}, age: ${ageInMinutes} minutes")

                if (file.lastModified() < oneHourAgo) {
                    val deleted = file.delete()
                    if (deleted) {
                        deletedCount++
                        Log.d(TAG, "Deleted old palm image: ${file.name}")
                    } else {
                        Log.w(TAG, "Failed to delete old palm image: ${file.name}")
                    }
                } else {
                    Log.d(TAG, "Keeping recent palm image: ${file.name}")
                }
            }

            Log.d(
                TAG,
                "Cleanup completed. Deleted $deletedCount out of ${palmFiles.size} palm images"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old palm images", e)
        }
    }

    private fun getFileFromUri(uri: Uri, context: Context): File? {
        return try {
            Log.d(TAG, "Converting URI to file: $uri")

            // For FileProvider URIs, we need to extract the actual file path
            val authority = "${context.packageName}.fileprovider"
            if (uri.authority == authority) {
                // This is a FileProvider URI, get the actual file path
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                if (storageDir != null) {
                    // Extract filename from URI path
                    val uriPath = uri.path
                    if (uriPath != null && uriPath.contains("PALM_")) {
                        val fileName = uriPath.substring(uriPath.lastIndexOf("/") + 1)
                        val file = File(storageDir, fileName)
                        Log.d(TAG, "Resolved file path: ${file.absolutePath}")
                        return file
                    }
                }
            }

            // Fallback: try to get file directly from path
            val path = uri.path
            if (path != null && path.contains("PALM_")) {
                val file = File(path)
                Log.d(TAG, "Using direct file path: ${file.absolutePath}")
                return file
            }

            Log.w(TAG, "Could not resolve file from URI: $uri")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file from URI: $uri", e)
            null
        }
    }
}
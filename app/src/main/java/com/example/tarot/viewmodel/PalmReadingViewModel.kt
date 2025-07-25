package com.example.tarot.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.repository.PalmReadingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

class PalmReadingViewModel(
    private val palmReadingRepository: PalmReadingRepository
) : ViewModel() {

    private fun cleanupImageFile(imageUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.example.tarot.util.CameraUtils.cleanupImageFile(imageUri, context)
            } catch (e: Exception) {
                Log.e(tag, "Error during image cleanup", e)
            }
        }
    }

    private val _uiState = MutableStateFlow(PalmReadingUiState())
    val uiState: StateFlow<PalmReadingUiState> = _uiState.asStateFlow()

    private val tag = "PalmReadingViewModel"

    fun analyzePalmImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val imageBase64 = convertImageToBase64(imageUri, context)
                val result = palmReadingRepository.analyzePalmImage(imageBase64)

                result.fold(
                    onSuccess = { reading ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            palmReading = reading,
                            imageUri = imageUri
                        )
                        Log.d(tag, "Palm reading completed successfully")

                        // Clean up temporary image file after successful processing
                        cleanupImageFile(imageUri, context)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                        Log.e(tag, "Palm reading failed", exception)

                        // Clean up temporary image file even on failure
                        cleanupImageFile(imageUri, context)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to process image"
                )
                Log.e(tag, "Image processing failed", e)

                // Clean up temporary image file on processing error
                cleanupImageFile(imageUri, context)
            }
        }
    }

    private suspend fun convertImageToBase64(uri: Uri, context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Could not read image")
                inputStream.close()

                // Compress image if needed
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val compressedBytes = compressBitmap(bitmap)

                Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                Log.e(tag, "Error converting image to base64", e)
                throw e
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()

        // Resize if image is too large
        val maxSize = 1024
        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val ratio = min(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    fun clearReading() {
        _uiState.value = PalmReadingUiState()
        Log.d(tag, "Palm reading cleared")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        Log.d(tag, "Error cleared")
    }
}

data class PalmReadingUiState(
    val isLoading: Boolean = false,
    val palmReading: String? = null,
    val imageUri: Uri? = null,
    val error: String? = null
)
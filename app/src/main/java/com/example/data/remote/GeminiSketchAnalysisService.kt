package com.example.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.remote.model.GeminiContent
import com.example.data.remote.model.GeminiGenerateRequest
import com.example.data.remote.model.GeminiGenerateResponse
import com.example.data.remote.model.GeminiGenerationConfig
import com.example.data.remote.model.GeminiInlineData
import com.example.data.remote.model.GeminiPart
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

interface GeminiApiEndpoint {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

/**
 * Service to analyze UI sketch images using the Gemini API.
 */
class GeminiSketchAnalysisService(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApiEndpoint = retrofit.create(GeminiApiEndpoint::class.java)

    /**
     * Analyzes a UI sketch image file using the Gemini API (gemini-3.5-flash) and returns a basic layout description.
     */
    suspend fun analyzeSketch(
        imageFile: File,
        customPrompt: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!imageFile.exists()) {
                return@withContext Result.failure(
                    IllegalArgumentException("File gambar tidak ditemukan di: ${imageFile.absolutePath}")
                )
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API Key belum dikonfigurasi. Harap masukkan API key di panel Secrets AI Studio.")
                )
            }

            val base64Data = encodeImageToBase64(imageFile)
            val promptText = customPrompt ?: DEFAULT_SKETCH_PROMPT

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = promptText),
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Data
                                )
                            )
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    topP = 0.95f
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = "You are an expert UI engineer and wireframe analyst. Analyze user UI sketches and provide structured layout descriptions."
                        )
                    )
                )
            )

            val response = api.generateContent(apiKey = apiKey, request = request)

            val candidateText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.mapNotNull { it.text }
                ?.joinToString("\n")

            if (!candidateText.isNullOrBlank()) {
                Result.success(candidateText)
            } else {
                val errorMsg = response.error?.message ?: "Tidak ada respons deskripsi layout yang diterima dari Gemini API."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyzes a UI sketch image and directly parses the result into a structured [MockUiLayout].
     */
    suspend fun analyzeAndParseSketch(
        imageFile: File,
        customPrompt: String? = null
    ): Result<com.example.data.model.MockUiLayout> = withContext(Dispatchers.IO) {
        analyzeSketch(imageFile, customPrompt).map { rawDescription ->
            com.example.utils.SketchLayoutParser.parseSketchLayoutDescription(rawDescription)
        }
    }

    private fun encodeImageToBase64(imageFile: File): String {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: throw IllegalArgumentException("Gagal mendecode file gambar menjadi Bitmap")

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    companion object {
        const val DEFAULT_SKETCH_PROMPT = """
Analyze this UI sketch / wireframe drawing and provide a concise, structured layout description:

1. **Screen Type & Objective**: Identify the intended purpose (e.g., Dashboard, Form, Analytics, Detail View).
2. **Top Navigation & App Bar**: Header title, actions, profile or menu icons.
3. **Primary Layout Containers**: Sections, cards, lists, grids, or hero elements.
4. **Interactive Elements & Controls**: Buttons, text fields, chips, tabs, or toggles.
5. **Suggested Compose Layout Hierarchy**: Recommended Compose layout containers (e.g. Scaffold -> Column -> LazyColumn / Cards / Buttons).

Provide a clear and well-formatted layout description.
"""
    }
}

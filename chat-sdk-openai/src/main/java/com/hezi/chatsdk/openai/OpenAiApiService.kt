package com.hezi.chatsdk.openai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenAiApiService {
    
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): Response<OpenAiChatResponse>
    
    @Streaming
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun chatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): Response<okhttp3.ResponseBody>
}


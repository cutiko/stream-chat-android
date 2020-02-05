package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.CompletableResponse
import io.getstream.chat.android.client.UploadFileResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitCdnApi {
    @Multipart
    @POST("/channels/{type}/{id}/image")
    fun sendImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): Call<UploadFileResponse>

    @Multipart
    @POST("/channels/{type}/{id}/file")
    fun sendFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): Call<UploadFileResponse>

    @DELETE("/channels/{type}/{id}/file")
    fun deleteFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Query("url") url: String
    ): Call<CompletableResponse>

    @DELETE("/channels/{type}/{id}/image")
    fun deleteImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Query("url") url: String
    ): Call<CompletableResponse>
}
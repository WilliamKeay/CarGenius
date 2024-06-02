package com.example.cargenius

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SpotifyAuthManager(
    private val context: Context,
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String,
    private val requestCode: Int
) {
    private val TAG = "SpotifyAuthManager"

    fun authenticateSpotify() {
        val builder = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.CODE,
            redirectUri
        )
        builder.setScopes(arrayOf("user-read-playback-state", "user-modify-playback-state", "user-read-currently-playing"))
        val request = builder.build()
        AuthorizationClient.openLoginActivity(context as AppCompatActivity, requestCode, request)
    }

    fun handleAuthResponse(
        resultCode: Int,
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val response = AuthorizationClient.getResponse(resultCode, data)
        when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                exchangeCodeForToken(response.code, onSuccess, onError)
            }
            AuthorizationResponse.Type.ERROR -> {
                onError(response.error)
            }
            else -> {
                onError("Unknown response type")
            }
        }
    }

    private fun exchangeCodeForToken(
        code: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val authUrl = "https://accounts.spotify.com/api/token"
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri)
            .build()

        val request = Request.Builder()
            .url(authUrl)
            .addHeader("Authorization", "Basic " + getBase64AuthHeader())
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to exchange code for token: ${e.message}")
                onError(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val accessToken = JSONObject(json).getString("access_token")
                    onSuccess(accessToken)
                } else {
                    Log.e(TAG, "Failed to exchange code for token: ${response.code}")
                    onError("Failed to exchange code for token: ${response.code}")
                }
            }
        })
    }

    private fun getBase64AuthHeader(): String {
        val auth = "$clientId:$clientSecret"
        return Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
    }
}

package com.example.cargenius

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class AuthenticationManager(private val activity: AppCompatActivity) {

    private val REQUEST_CODE = 1337
    private val clientId = "aa9a2e56883c4ea8b11a15a919f53787"
    private val redirectUri = "http://localhost:8888/callback"

    fun authenticateSpotify() {
        val builder = AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, redirectUri)
        builder.setScopes(arrayOf("user-read-playback-state", "user-modify-playback-state", "user-read-currently-playing"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Successfully authenticated
                    val accessToken = response.accessToken
                    return true
                }
                AuthorizationResponse.Type.ERROR -> {
                    // Handle error
                    Log.e("MainActivity", "Auth error: ${response.error}")
                }
                else -> {
                    // Handle other cases
                }
            }
        }
        return false
    }
}

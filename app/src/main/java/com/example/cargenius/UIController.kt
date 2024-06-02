package com.example.cargenius

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationResponse

class UIController(private val activity: AppCompatActivity, private val authenticationManager: AuthenticationManager, private val spotifyManager: SpotifyManager) {

    init {
        authenticationManager.authenticateSpotify()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return authenticationManager.onActivityResult(requestCode, resultCode, data)
    }

    fun openWazeNavigation() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("waze://?navigate=yes"))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Waze app is not installed: ${e.message}")
        }
    }

    fun connectSpotify(accessToken: String) {
        spotifyManager.connectToSpotifyAppRemote(accessToken)
    }
}

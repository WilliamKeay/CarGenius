package com.example.cargenius

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException



class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1337
    //private var accessToken: String? = null
    private val clientId = "ENTER_YOUR_CLIENT_ID"
    private val redirectUri = "ENTER_YOUR_REDIRECT_URL_EG_LOCALHOST:****"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val playlistURI = "ENTER_YOUR_PLAYLIST_URL"
    private val clientSecret = "ENTER_YOUR_CLIENT_SECRET"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView2: ImageView = findViewById(R.id.imageView2)

        // Set OnClickListener for imageView2
        imageView2.setOnClickListener {
            settings() // Call your method here
        }
    }

    fun settings() {

    }

    override fun onStart() {
        super.onStart()
        authenticateSpotify()
    }

    private fun exchangeCodeForToken(code: String) {
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
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Failed to exchange code for token: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val accessToken = JSONObject(json).getString("access_token")

                    // Now connect to Spotify App Remote using the access token
                    connectToSpotifyAppRemote(accessToken)
                } else {
                    Log.e(TAG, "Failed to exchange code for token: ${response.code}")
                }
            }
        })
    }


    private fun getBase64AuthHeader(): String {
        val auth = "$clientId:$clientSecret"
        return android.util.Base64.encodeToString(auth.toByteArray(), android.util.Base64.NO_WRAP)
    }


    private fun authenticateSpotify() {
        val builder = AuthorizationRequest.Builder(clientId, AuthorizationResponse.Type.TOKEN, redirectUri)
        builder.setScopes(arrayOf("user-read-playback-state", "user-modify-playback-state", "user-read-currently-playing"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Successfully authenticated
                    val accessToken = response.accessToken
                    connectToSpotifyAppRemote(accessToken)
                    playPlaylist()
                    setShuffle()
                    openWazeNavigation() // Attempt to open Waze even if shuffle fails
                }
                AuthorizationResponse.Type.ERROR -> {
                    // Handle error
                    Log.e("MainActivity", "Auth error: ${response.error}")
                    openWazeNavigation() // Attempt to open Waze even if shuffle fails
                }
                else -> {
                    // Handle other cases
                }
            }
        }
    }

    private fun openWazeNavigation() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("waze://?navigate=yes"))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Waze app is not installed: ${e.message}")
            // Handle the case where Waze is not installed
        }
    }

    private fun playPlaylist() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.play(playlistURI)
                .setResultCallback { setResult ->
                    Log.d("MainActivity", "Play result: ${setResult.javaClass.simpleName}")
                    // Check if there's a specific success condition you want to handle
                }
                .setErrorCallback { error ->
                    Log.e("MainActivity", "Error playing playlist: ${error.message}")
                }
        }
    }

    private fun setShuffle() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.setShuffle(true)
                .setResultCallback { setResult ->
                    Log.d("MainActivity", "Shuffle result: ${setResult.javaClass.simpleName}")
                    // Check if there's a specific success condition you want to handle
                }
                .setErrorCallback { error ->
                    Log.e("MainActivity", "Error setting shuffle mode: ${error.message}")
                }
        }
    }

    private fun connectToSpotifyAppRemote(accessToken: String) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d(TAG, "Connected to Spotify")
                spotifyAppRemote = appRemote
                playPlaylist()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "Failed to connect to Spotify: ${throwable.message}", throwable)
            }
        })
    }


    private fun connected() {
        spotifyAppRemote?.let { remote ->
            // Play a playlist
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            remote.playerApi.play(playlistURI)

            // Subscribe to PlayerState
            remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                val track: Track = playerState.track
                Log.d("MainActivity", "${track.name} by ${track.artist.name}")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}
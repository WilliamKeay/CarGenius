package com.example.cargenius

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

class SpotifyManager(private val context: Context) {

    private val clientId = "aa9a2e56883c4ea8b11a15a919f53787"
    private val redirectUri = "http://localhost:8888/callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val playlistURI = "spotify:playlist:48M0yBX95RvwVAOypLJggg"

    fun connectToSpotifyAppRemote(accessToken: String) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d("MainActivity", "Connected to Spotify")
                spotifyAppRemote = appRemote
                playPlaylist()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", "Failed to connect to Spotify: ${throwable.message}", throwable)
            }
        })
    }

    fun playPlaylist() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.play(playlistURI)
                .setResultCallback { setResult ->
                    Log.d("MainActivity", "Play result: ${setResult.javaClass.simpleName}")
                }
                .setErrorCallback { error ->
                    Log.e("MainActivity", "Error playing playlist: ${error.message}")
                }
        }
    }

    fun setShuffle() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.setShuffle(true)
                .setResultCallback { setResult ->
                    Log.d("MainActivity", "Shuffle result: ${setResult.javaClass.simpleName}")
                }
                .setErrorCallback { error ->
                    Log.e("MainActivity", "Error setting shuffle mode: ${error.message}")
                }
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}

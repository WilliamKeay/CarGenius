package com.example.cargenius

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Repeat

class SpotifyPlaybackManager(
    private val context: Context,
    private val clientId: String,
    private val redirectUri: String
) {
    private val TAG = "SpotifyPlaybackManager"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    fun connectToSpotifyAppRemote(accessToken: String, onConnected: () -> Unit, onError: (Throwable) -> Unit) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d(TAG, "Connected to Spotify")
                spotifyAppRemote = appRemote
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "Failed to connect to Spotify: ${throwable.message}", throwable)
                onError(throwable)
            }
        })
    }

    fun playPlaylist(playlistURI: String) {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.play(playlistURI)
                .setResultCallback { setResult ->
                    Log.d(TAG, "Play result: ${setResult.javaClass.simpleName}")
                }
                .setErrorCallback { error ->
                    Log.e(TAG, "Error playing playlist: ${error.message}")
                }
        }
    }

    fun setShuffle(shuffle: Boolean) {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.setShuffle(shuffle)
                .setResultCallback { setResult ->
                    Log.d(TAG, "Shuffle result: ${setResult.javaClass.simpleName}")
                }
                .setErrorCallback { error ->
                    Log.e(TAG, "Error setting shuffle mode: ${error.message}")
                }
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}

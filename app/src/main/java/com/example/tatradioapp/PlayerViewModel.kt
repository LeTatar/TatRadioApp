@file:Suppress("DEPRECATION")
package com.example.tatradioapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.exoplayer2.ExoPlayer

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    var player: ExoPlayer? = null

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}
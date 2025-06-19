package com.example.tatradioapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.exoplayer.ExoPlayer

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    var player: ExoPlayer? = null

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }
}
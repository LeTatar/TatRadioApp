@file:Suppress("DEPRECATION")
package viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlayer

class PlayerViewModel : ViewModel() {

    var player: ExoPlayer? = null

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}

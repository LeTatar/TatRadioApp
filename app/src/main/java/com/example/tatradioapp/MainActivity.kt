package com.example.tatradioapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var statusTextView: TextView
    private lateinit var stationNameTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar

    private val stations = mapOf(
        R.id.buttonBulgar to Station("Болгар радиосы", "https://live.bolgarradio.com/b_aac_hifi.m3u8"),
        R.id.buttonTartip to Station("Тәртип FM", "https://radio.tatmedia.com:8443/tartipfm"),
        R.id.buttonTatarRadio to Station("Татар радиосы", "https://tatarradio.hostingradio.ru/tatarradio320.mp3"),
        R.id.buttonKunel to Station("Күңел радиосы", "http://radio.tatmedia.com:8800/aktanysh"),
        R.id.buttonKurai to Station("Курай радиосы", "https://av.bimradio.ru/kurai_mp3"),
        R.id.buttonKitapFM to Station("Китап FM", "https://radio.tatmedia.com:8443/kitapfm")
    )

    data class Station(val name: String, val url: String)

    private var currentPlayingLayout: LinearLayout? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        stationNameTextView = findViewById(R.id.stationNameTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        playerViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(PlayerViewModel::class.java)

        if (playerViewModel.player == null) {
            playerViewModel.player = ExoPlayer.Builder(this).build()
        }
        val player = playerViewModel.player!!

        stations.keys.forEach { layoutId ->
            val layout = findViewById<LinearLayout>(layoutId)
            layout.setOnClickListener {
                val station = stations[layoutId] ?: return@setOnClickListener
                if (currentPlayingLayout == layout && isPlaying) {
                    stopStream(player)
                    isPlaying = false
                    layout.alpha = 1.0f
                    currentPlayingLayout = null
                } else {
                    startStream(station, player)
                    isPlaying = true
                    currentPlayingLayout?.alpha = 1.0f
                    layout.alpha = 0.5f
                    currentPlayingLayout = layout
                }
            }
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        statusTextView.text = "Загрузка..."
                        loadingProgressBar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        statusTextView.text = "Воспроизведение"
                        loadingProgressBar.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        statusTextView.text = "Воспроизведение завершено"
                        loadingProgressBar.visibility = View.GONE
                        currentPlayingLayout?.alpha = 1.0f
                        currentPlayingLayout = null
                        isPlaying = false
                        stationNameTextView.text = ""
                    }
                    Player.STATE_IDLE -> {
                        statusTextView.text = "Остановлено"
                        loadingProgressBar.visibility = View.GONE
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                loadingProgressBar.visibility = View.GONE
                currentPlayingLayout?.alpha = 1.0f
                currentPlayingLayout = null
                isPlaying = false
                stationNameTextView.text = ""

                // Проверяем, является ли ошибка ошибкой источника с HTTP кодом 404
                val cause = error.cause
                if (cause is HttpDataSource.InvalidResponseCodeException) {
                    if (cause.responseCode == 404) {
                        statusTextView.text = "Ошибка: поток не найден (404)"
                        return
                    }
                }
                statusTextView.text = "Ошибка воспроизведения: ${error.message}"
            }
        })
    }

    private fun startStream(station: Station, player: ExoPlayer) {
        try {
            stopStream(player)
            stationNameTextView.text = station.name
            val mediaItem = MediaItem.fromUri(Uri.parse(station.url))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } catch (e: Exception) {
            statusTextView.text = "Ошибка: ${e.message}"
            loadingProgressBar.visibility = View.GONE
        }
    }

    private fun stopStream(player: ExoPlayer) {
        if (player.isPlaying) {
            player.stop()
            player.clearMediaItems()
        }
        statusTextView.text = "Остановлено"
        stationNameTextView.text = ""
        loadingProgressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        // playerViewModel.player?.release() вызывается в ViewModel.onCleared()
    }
}
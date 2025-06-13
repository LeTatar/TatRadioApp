package com.example.tatradioapp

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/* class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TatRadioAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    TatRadioAppTheme {
        Greeting("Android")
    }
}

@Composable
fun TatRadioAppTheme(content: @Composable () -> Unit) {
    val nothing = TODO("Not yet implemented")
}
*/
class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var statusTextView: TextView

    // Ссылки на радиостанции
    private val stations = mapOf(
        R.id.buttonBulgar to "https://live.bolgarradio.com/b_aac_hifi.m3u8",
        R.id.buttonTartip to "https://radio.tatmedia.com:8443/tartipfm",
        R.id.buttonTatarRadio to "https://tatarradio.hostingradio.ru/tatarradio320.mp3",
        R.id.buttonKunel to "https://radio.tatmedia.com:8443/kunel",
        R.id.buttonKurai to "https://av.bimradio.ru/kurai_mp3",
        R.id.buttonKitapFM to "https://radio.tatmedia.com:8443/kitapfm"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)

        // Инициализация MediaPlayer с аудио атрибутами
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }

        // Назначаем обработчики кнопкам радиостанций
        stations.keys.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                val url = stations[buttonId]
                url?.let {
                    playStream(it)
                }
            }
        }

        // Кнопка Стоп
        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            stopStream()
        }
    }

    private fun playStream(url: String) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset() // Сброс перед новым источником
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            statusTextView.text = "Загрузка..."
            mediaPlayer.setOnPreparedListener {
                it.start()
                statusTextView.text = "Воспроизведение"
            }
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                // what и extra — коды ошибок, можно их логировать или показывать пользователю
                Log.e("MediaPlayerError", "Error occurred: what=$what, extra=$extra")
                statusTextView.text = "Ошибка воспроизведения: what=$what, extra=$extra"
                true  // true — ошибка обработана
            }
            mediaPlayer.setOnInfoListener { mp, what, extra ->
                // what и extra — коды информационных событий
                Log.i("MediaPlayerInfo", "Info: what=$what, extra=$extra")
                // Можно обновлять UI или реагировать на буферизацию и т.п.
                false // false — событие не обработано полностью, передается дальше
            }
        } catch (e: Exception) {
            statusTextView.text = "Ошибка: ${e.message}"
        }
    }

    private fun stopStream() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            statusTextView.text = "Стоп"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
*/
class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var statusTextView: TextView

    // Класс для хранения имени и URL радиостанции
    data class Station(val name: String, val url: String)

    // Ссылки на радиостанции с именами
    private val stations = mapOf(
        R.id.buttonBulgar to Station("Булгар радиосы", "https://live.bolgarradio.com/b_aac_hifi.m3u8"),
        R.id.buttonTartip to Station("Тартип FM", "https://radio.tatmedia.com:8443/tartipfm"),
        R.id.buttonTatarRadio to Station("Татар радиосы", "https://tatarradio.hostingradio.ru/tatarradio320.mp3"),
        R.id.buttonKunel to Station("Кунел радиосы", "https://radio.tatmedia.com:8443/kunel"),
        R.id.buttonKurai to Station("Курай радиосы", "https://av.bimradio.ru/kurai_mp3"),
        R.id.buttonKitapFM to Station("Китап FM", "https://radio.tatmedia.com:8443/kitapfm")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }

        // Назначаем обработчики кнопкам радиостанций
        stations.keys.forEach { buttonId ->
            findViewById<Button>(buttonId).setOnClickListener {
                val station = stations[buttonId]
                station?.let {
                    playStream(it.url, it.name)
                }
            }
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            stopStream()
        }
    }

    // Добавлен параметр stationName для отображения названия станции
    private fun playStream(url: String, stationName: String) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            statusTextView.text = "Загрузка..."
            mediaPlayer.setOnPreparedListener {
                it.start()
                statusTextView.text = "Воспроизведение: $stationName"
            }
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayerError", "Error occurred: what=$what, extra=$extra")
                statusTextView.text = "Ошибка воспроизведения: what=$what, extra=$extra"
                true
            }
            mediaPlayer.setOnInfoListener { mp, what, extra ->
                Log.i("MediaPlayerInfo", "Info: what=$what, extra=$extra")
                false
            }
        } catch (e: Exception) {
            statusTextView.text = "Ошибка: ${e.message}"
        }
    }

    private fun stopStream() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            statusTextView.text = "Стоп"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
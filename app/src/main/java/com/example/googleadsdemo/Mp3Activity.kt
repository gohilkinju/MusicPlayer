package com.example.googleadsdemo

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.TimeUnit

class Mp3Activity : AppCompatActivity() {
    private var songList: MutableList<String> = mutableListOf()
    private var currentIndex = 0

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var txtsongname: TextView
    private lateinit var textTotalTime: TextView
    private lateinit var imgposter: ImageView
    private lateinit var buttonPlay: ImageView
    private lateinit var buttonPause: ImageView
    private lateinit var backbutton: ImageView
    private lateinit var buttonNext: ImageView
    private lateinit var buttonprevious: ImageView

    private val handler = Handler(Looper.getMainLooper())

    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                textCurrentTime.text = formatTime(mediaPlayer.currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mp3)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mp3activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        txtsongname = findViewById(R.id.txtsongname)
        textTotalTime = findViewById(R.id.textTotalTime)
        buttonPlay = findViewById(R.id.imgplay)
        backbutton = findViewById(R.id.backbutton)
        imgposter = findViewById(R.id.imgposter)
        buttonPause = findViewById(R.id.imgpausebutton)
        buttonNext = findViewById(R.id.imgnext)
        buttonprevious = findViewById(R.id.imgpre)

        // Load all songs from device
//        songList = loadAllSongs()

        val intentSongs = intent.getStringArrayListExtra("song_list")
        val intentIndex = intent.getIntExtra("song_index", 0)

        if (intentSongs != null) {
            songList.clear()
            songList.addAll(intentSongs)
            currentIndex = intentIndex
        }

        // Initialize player with selected song
        if (songList.isNotEmpty()) {
            initPlayer(songList[currentIndex])
        }

        buttonPlay.setOnClickListener {
            mediaPlayer.start()
            handler.post(updateSeekBar)
            buttonPlay.visibility = View.GONE
            buttonPause.visibility = View.VISIBLE
        }

        backbutton.setOnClickListener { finish() }

        buttonPause.setOnClickListener {
            mediaPlayer.pause()
            buttonPlay.visibility = View.VISIBLE
            buttonPause.visibility = View.GONE
        }

        buttonNext.setOnClickListener { playNextSong() }
        buttonprevious.setOnClickListener { playPreviousSong() }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    textCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playNextSong() {
        if (songList.isEmpty()) return
        mediaPlayer.stop()
        mediaPlayer.release()

        currentIndex = (currentIndex + 1) % songList.size
        initPlayer(songList[currentIndex])
        mediaPlayer.start()
        handler.post(updateSeekBar)
        buttonPlay.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE

    }
    private fun playPreviousSong() {
        if (songList.isEmpty()) return
        mediaPlayer.stop()
        mediaPlayer.release()

        currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        initPlayer(songList[currentIndex])
        mediaPlayer.start()
        handler.post(updateSeekBar)
        buttonPlay.visibility = View.GONE
        buttonPause.visibility = View.VISIBLE
    }
    private fun initPlayer(path: String) {
        txtsongname.text = path.substringAfterLast("/") // Show file name
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val artBytes = retriever.embeddedPicture
        if (artBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            imgposter.setImageBitmap(bitmap)
        } else {
            imgposter.setImageResource(R.drawable.background)
        }
        retriever.release()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepare()
        seekBar.max = mediaPlayer.duration
        textTotalTime.text = formatTime(mediaPlayer.duration)
    }

    private fun loadAllSongs(): MutableList<String> {
        val songs = mutableListOf<String>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                songs.add(it.getString(columnIndex))
            }
        }
        return songs
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}

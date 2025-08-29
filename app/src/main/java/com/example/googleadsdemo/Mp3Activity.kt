package com.example.googleadsdemo

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.view.View
import android.view.WindowManager
import java.util.concurrent.TimeUnit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Mp3Activity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    // Declare UI elements
    private lateinit var seekBar: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var txtsongname: TextView
    private lateinit var textTotalTime: TextView

    private lateinit var imgposter: ImageView
    private lateinit var buttonPlay: ImageView
    private lateinit var buttonPause: ImageView
    private lateinit var backbutton: ImageView
    private lateinit var buttonStop: ImageView

    private val handler = Handler(Looper.getMainLooper())



    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {

                // Update SeekBar progress and current time text
                seekBar.progress = mediaPlayer.currentPosition
                textCurrentTime.text = formatTime(mediaPlayer.currentPosition)


                // Repeat this task every 1 second
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


        // Initialize views from layout
        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        txtsongname = findViewById(R.id.txtsongname)
        textTotalTime = findViewById(R.id.textTotalTime)
        buttonPlay = findViewById(R.id.imgplay)
        backbutton = findViewById(R.id.backbutton)
        imgposter = findViewById(R.id.imgposter)
        buttonPause = findViewById(R.id.imgpausebutton)
//        buttonStop = findViewById(R.id.buttonStop)

        val musicPath = intent.getStringExtra("music_path")
        val musicName = intent.getStringExtra("music_name")

        txtsongname.text = musicName

        // Create MediaPlayer instance with a raw audio resource

        if (musicPath != null) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(musicPath)

            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                imgposter.setImageBitmap(bitmap)
            } else {
                imgposter.setImageResource(R.drawable.background) // fallback
            }
            retriever.release()
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicPath)
            prepare()
        }
        // Set listener to configure SeekBar and total time after MediaPlayer is ready
        mediaPlayer.setOnPreparedListener {
            seekBar.max = it.duration
            textTotalTime.text = formatTime(it.duration)

        }

        buttonPlay.setOnClickListener {
            mediaPlayer.start() // resumes if paused
            handler.post(updateSeekBar)
            buttonPlay.visibility = View.GONE
            buttonPause.visibility = View.VISIBLE
        }

        backbutton.setOnClickListener{
            finish()
        }
        buttonPause.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(musicPath)
            mediaPlayer.prepare()
            seekBar.progress = 0
            textCurrentTime.text = "0:00"
            buttonPlay.visibility = View.VISIBLE
            buttonPause.visibility = View.GONE
        }


        // Stop button stops playback and resets UI and MediaPlayer
//        buttonStop.setOnClickListener {
//            mediaPlayer.stop()
//            mediaPlayer = MediaPlayer.create(this, R.raw.birds)
//            seekBar.progress = 0
//            textCurrentTime.text = "0:00"
//            textTotalTime.text = formatTime(mediaPlayer.duration)
//        }

        // Listen for SeekBar user interaction
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            // Called when progress is changed
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Seek MediaPlayer to new position and update current time
                    mediaPlayer.seekTo(progress)
                    textCurrentTime.text = formatTime(progress)
                }
            }

            // Not used, but required to override
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            // Not used, but required to override
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    // Format milliseconds into minutes:seconds format (e.g., 1:05)
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    // Clean up MediaPlayer and handler when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
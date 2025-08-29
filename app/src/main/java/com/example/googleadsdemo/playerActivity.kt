package com.example.googleadsdemo

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class playerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicList: List<MusicModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.playerActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    1
                )
            } else {
                loadMusic()
            }
        }
        else {
            // Android 12 or lower
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                loadMusic()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusic()
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMusic() {
        musicList = getAllAudioFiles()
        Log.d("TAG", "loadMusic: ")
        recyclerView.adapter = MusicAdapter(musicList) { selectedMusic ->
            playMusic(selectedMusic.path)
        }
//        recyclerView.adapter = MusicAdapter(musicList) { selectedMusic ->
//
//        }

    }
    private fun getAllAudioFiles(): List<MusicModel> {
        val list = mutableListOf<MusicModel>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor = contentResolver.query(uri, null, selection, null, null)

        cursor?.use {
            val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val pathCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumIdCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID,)
            val idCol = it.getColumnIndex(MediaStore.Audio.Media._ID)

            while (it.moveToNext()) {
                val title = it.getString(titleCol)
                val artist = it.getString(artistCol)
                val path = it.getString(pathCol)
                val albumId = it.getLong(albumIdCol)
                val id = it.getLong(idCol)

                val albumArtUri = Uri.parse("content://media/external/audio/albumart")
                    .buildUpon()
                    .appendPath(albumId.toString())
                    .build()

                list.add(MusicModel(id, title, artist, path,albumArtUri))
            }
        }
        return list
    }

    private fun playMusic(path: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
        Toast.makeText(this, "Playing...", Toast.LENGTH_SHORT).show()
    }



    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}
package com.example.googleadsdemo

import android.net.Uri

data class MusicModel(
    val id: Long,
    val title: String,
    val artist: String,
    val path: String,
    val albumArtUri: Uri?
)


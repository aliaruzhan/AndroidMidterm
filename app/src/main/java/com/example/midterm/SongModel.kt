package com.example.midterm

import java.io.Serializable

data class SongModel(
    var resId: Int,
    var songTitle: String,
    var songArtist: String,
    var image: Int,
) : Serializable
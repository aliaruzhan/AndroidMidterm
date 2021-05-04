package com.example.midterm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_lyrics.*


class LyricsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val music = intent.getSerializableExtra("song") as SongModel
        lyrics.text = application.assets.open("${music.songTitle}.txt").bufferedReader().readText()
        lyrics.movementMethod = ScrollingMovementMethod()
        songName.text = music.songTitle
        author.text = music.songArtist
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true;
    }
}
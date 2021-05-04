package com.example.midterm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.track_fragment.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "Midterm"
    private val notificationId = 101
    private lateinit var prefs: SharedPreferences
    private var music: ArrayList<SongModel> = ArrayList()
    private var player: MediaPlayer? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable:Runnable
    private lateinit var metadataRetriever: MediaMetadataRetriever
    private var currentSongId = -1
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        metadataRetriever = MediaMetadataRetriever()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getMusicFromRawStorage()

        play_pause_btn.setOnClickListener {
            if (player!!.isPlaying) {
                player!!.pause()
                play_pause_btn.text = "play"
                sendNotification()
            } else {
                player!!.start()
                play_pause_btn.text = "pause"
            }
        }
        prev_btn.setOnClickListener {
            playPrevious()
        }
        next_song.setOnClickListener {
            playNext()
        }
        lyrics_btn.setOnClickListener {
            openLyrics()
        }
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun sendNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentTitle("Media Player")
            .setContentText("Music Paused")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }

    private fun openLyrics() {

        val intent = Intent(this, LyricsActivity::class.java).apply {
            putExtra("song", music[currentSongId])
        }
        startActivity(intent)
    }

    private fun playNext() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId++
            if (currentSongId >= music.size) {
                currentSongId = 0
            }
            initMusicPlayer(music[currentSongId])
            player!!.start()
        }
    }

    private fun playPrevious() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId--
            if (currentSongId < 0) {
                currentSongId = music.size - 1
            }
            initMusicPlayer(music[currentSongId])
            player!!.start()
        }
    }

    private fun getMusicFromRawStorage() {
        val songList = listOf(
            R.raw.breathe,
            R.raw.chasing_fire,
            R.raw.like_me_better,
            R.raw.other,
            R.raw.reforget,
            )

        for (rId in songList) {
            val mediaPath = Uri.parse("android.resource://$packageName/$rId")
            metadataRetriever.setDataSource(this, mediaPath)

            val songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "none"
            val songArtist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "none"
            val image = R.drawable.lauv
            val model = SongModel(rId, songTitle, songArtist, image)
            music.add(model)
        }
        if (music.size > 0) {
            musicList.visibility = View.VISIBLE
            setupMusicListRecyclerView()
        } else {
            musicList.visibility = View.GONE
        }
    }

    private fun setupMusicListRecyclerView() {
        musicList.layoutManager = LinearLayoutManager(this)
        musicList.setHasFixedSize(true)

        val musicAdapter = SongAdapter(this, music)
        musicList.adapter = musicAdapter

        musicAdapter.setOnClickListener(object : SongAdapter.OnClickListener {
            override fun onClick(position: Int, model: SongModel) {
                if (player != null) {
                    player!!.stop()
                }
                if (included_layout.visibility == View.GONE) {
                    included_layout.visibility = View.VISIBLE
                }
                initMusicPlayer(model)
                currentSongId = position
                player!!.start()

            }
        })
    }

    private fun initMusicPlayer(model: SongModel) {
        player = MediaPlayer.create(this@MainActivity, model.resId)
        player!!.setOnCompletionListener {
            val delay = prefs.getString("delay", "0")!!.toInt()
            if (delay != 0) {
                songTitle.text = "Break time!"
                "${formatWithLeadingZeroes(delay / 60)}:${formatWithLeadingZeroes(delay % 60)}"
                val breakTime = System.currentTimeMillis()
                runnable = Runnable {
                    val spendTime = ((System.currentTimeMillis() - breakTime) / 1000).toInt()
                    " ${formatWithLeadingZeroes(spendTime / 60)}:${formatWithLeadingZeroes(spendTime % 60)}"
                    if (spendTime < delay) {
                        handler.postDelayed(runnable, 1000)
                    } else {
                        playNext()
                    }
                }

            }
        }
    }
    private fun formatWithLeadingZeroes(number: Int): String {
        return number.toString().padStart(2, '0')
    }
}


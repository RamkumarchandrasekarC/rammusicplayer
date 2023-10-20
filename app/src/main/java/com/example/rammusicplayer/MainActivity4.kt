package com.example.rammusicplayer
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.ArrayAdapter
import com.squareup.picasso.Picasso
import android.content.pm.PackageManager
import java.io.File
import android.media.MediaMetadataRetriever


class MainActivity4 : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var songNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var currentTimeTextView: TextView
    private lateinit var totalDurationTextView: TextView
    private lateinit var buttonForward: ImageButton
    private lateinit var buttonBackward: ImageButton
    private lateinit var thumbnailImageView: ImageView
    private lateinit var songListView: ListView
    private lateinit var songListAdapter: ArrayAdapter<String>

    private var songName: String? = null
    private var artistName: String? = null
    private var filePath: String? = null
    private var albumId: Long = -1
    private var songList: List<String>? = null
    private var selectedPosition: Int = -1

    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    private val requestExternalStoragePermissionCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        mediaPlayer = MediaPlayer()

        // Retrieve data from intent
        songName = intent.getStringExtra(EXTRA_SONG_NAME)
        artistName = intent.getStringExtra(EXTRA_ARTIST_NAME)
        filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        albumId = intent.getLongExtra(EXTRA_ALBUM_ID, -1)
        songList = intent.getStringArrayListExtra(EXTRA_SONG_LIST)?.toList()
        selectedPosition = intent.getIntExtra("selectedPosition", -1)

        // Initialize UI components
        songNameTextView = findViewById(R.id.centeredSongName)
        artistNameTextView = findViewById(R.id.centeredArtistName)
        seekBar = findViewById(R.id.musicSeekBar)
        currentTimeTextView = findViewById(R.id.textCurrentTime)
        totalDurationTextView = findViewById(R.id.textTotalDuration)
        buttonForward = findViewById(R.id.buttonForward)
        buttonBackward = findViewById(R.id.buttonBackward)
        thumbnailImageView = findViewById(R.id.centeredThumbnail)
        songListView = findViewById(R.id.songListView)

        playPauseButton = findViewById(R.id.buttonPlayPause)
        playPauseButton.setOnClickListener {
            togglePlayback()
        }

        mediaPlayer.setOnCompletionListener {
            playNextSong()
        }

        setupSeekBar()

        buttonForward.setOnClickListener {
            playNextSong()
        }

        buttonBackward.setOnClickListener {
            playPreviousSong()
        }

        // Call updateUIWithCurrentSong to set initial values
        updateUIWithCurrentSong()

        if (isPlaying) {
            loadAlbumArt(thumbnailImageView, albumId)
        } else {
            thumbnailImageView.setImageResource(R.drawable.img_17)
        }

        // Set item click listener for the list view
        songListView.setOnItemClickListener { _, _, position, _ ->
            val selectedSongPath = songList?.get(position)
            selectedSongPath?.let { playSong(it, position) }
        }

        // Set up the song list adapter
        songListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songList ?: emptyList())
        songListView.adapter = songListAdapter

        // Find the index of the selected song and play it
        val selectedSongIndex = songList?.indexOf(filePath)
        if (selectedSongIndex != null && selectedSongIndex != -1) {
            playSong(filePath!!, selectedSongIndex)
        }
    }

    private fun setupSeekBar() {
        seekBar.max = mediaPlayer.duration

        handler.postDelayed(updateSeekBar, 1000)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    updateTimeTextViews()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateSeekBarUI() {
        seekBar.progress = mediaPlayer.currentPosition
        updateTimeTextViews()
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            updateSeekBarUI()
            handler.postDelayed(this, 1000)
        }
    }

    private fun togglePlayback() {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            requestExternalStoragePermission()
        }
        isPlaying = !isPlaying
        updatePlayPauseButtonImage()
    }

    private fun updatePlayPauseButtonImage() {
        val drawableResource = if (isPlaying) R.drawable.img_25 else R.drawable.img_1
        playPauseButton.setImageResource(drawableResource)

        if (isPlaying) {
            loadAlbumArt(thumbnailImageView, albumId)
        } else {
            thumbnailImageView.setImageResource(R.drawable.img_17)
        }
    }

    private fun updateTimeTextViews() {
        val currentTime = mediaPlayer.currentPosition
        val totalDuration = mediaPlayer.duration
        currentTimeTextView.text = formatTime(currentTime)
        totalDurationTextView.text = formatTime(totalDuration)
    }

    private fun formatTime(timeInMillis: Int): String {
        val minutes = timeInMillis / 1000 / 60
        val seconds = (timeInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playNextSong() {
        val currentIndex = selectedPosition

        if (currentIndex != -1 && currentIndex < songList!!.size - 1) {
            val nextSongPath = songList!![currentIndex + 1]
            val nextSongIndex = currentIndex + 1
            playSong(nextSongPath, nextSongIndex)
        }
    }

    private fun playPreviousSong() {
        val currentIndex = selectedPosition

        if (currentIndex != -1 && currentIndex > 0) {
            val previousSongPath = songList!![currentIndex - 1]
            playSong(previousSongPath, currentIndex - 1)
        }
    }

    private fun getSongNameFromPath(path: String): String {
        val fileName = File(path).nameWithoutExtension
        return fileName
    }

    private fun getArtistNameFromPath(path: String): String {
        val fileName = File(path).nameWithoutExtension
        return fileName.split(" - ").getOrNull(0) ?: "Unknown Artist"
    }

    private fun getAlbumIdFromPath(context: Context, path: String): Long {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, Uri.parse(path))
        val albumId = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        return albumId?.toLongOrNull() ?: -1L
    }


    private fun playSong(songPath: String, position: Int) {
        mediaPlayer.stop()
        mediaPlayer.reset()

        try {
            mediaPlayer.setDataSource(songPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            updatePlayPauseButtonImage()

            filePath = songPath // Update the current file path
            selectedPosition = position // Update the selected position

            // Retrieve song details for the new song
            // Replace this with your actual logic to get song details
            songName = getSongNameFromPath(songPath)
            artistName = getArtistNameFromPath(songPath)
            albumId = getAlbumIdFromPath(this, songPath)

            updateUIWithCurrentSong()

            seekBar.max = mediaPlayer.duration
            updateTimeTextViews()

            handler.postDelayed(updateSeekBar, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUIWithCurrentSong() {
        songNameTextView.text = songName ?: "Unknown Song"
        artistNameTextView.text = artistName ?: "Unknown Artist"

        // Use MediaMetadataRetriever to get album art
        val albumArt = getAlbumArtFromPath(filePath)

        if (albumArt != null) {
            thumbnailImageView.setImageBitmap(albumArt)
        } else {
            // Load a default image when album art is not available
            thumbnailImageView.setImageResource(R.drawable.img_17)
        }
    }

    private fun getAlbumArtFromPath(path: String?): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val rawArt = retriever.embeddedPicture
        retriever.release()

        return if (rawArt != null) {
            BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size)
        } else {
            null
        }
    }




    private fun loadAlbumArt(imageView: ImageView, albumId: Long) {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart").buildUpon()
            .appendPath(albumId.toString()).build()

        Picasso.get()
            .load(albumArtUri)
            .placeholder(R.drawable.img_17)
            .error(R.drawable.img_17)
            .into(imageView)
    }

    private fun requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mediaPlayer.start()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                requestExternalStoragePermissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestExternalStoragePermissionCode && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateSeekBar)
    }

    companion object {
        private const val EXTRA_SONG_NAME = "SONG_NAME"
        private const val EXTRA_ARTIST_NAME = "ARTIST_NAME"
        private const val EXTRA_FILE_PATH = "FILE_PATH"
        private const val EXTRA_ALBUM_ID = "ALBUM_ID"
        private const val EXTRA_SONG_LIST = "SONG_LIST"

        fun newIntent(
            context: Context,
            songName: String,
            artistName: String,
            filePath: String,
            albumId: Long,
            songList: List<String>,
            selectedPosition: Int
        ): Intent {
            return Intent(context, MainActivity4::class.java).apply {
                putExtra(EXTRA_SONG_NAME, songName)
                putExtra(EXTRA_ARTIST_NAME, artistName)
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_ALBUM_ID, albumId)
                putStringArrayListExtra(EXTRA_SONG_LIST, ArrayList(songList))
                putExtra("selectedPosition", selectedPosition)
            }
        }
    }
}




















package com.example.rammusicplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

data class SongModel(
    val songId: Long,
    val songName: String,
    val artistName: String,
    val albumId: Long,
    val filePath: String
)

class SongAdapter(
    private val songList: List<SongModel>,
    private val onItemClick: (SongModel, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val songName: TextView = itemView.findViewById(R.id.songName)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_songs, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]

        // Load album art using Picasso
        loadAlbumArt(holder.thumbnail, song.albumId)

        holder.songName.text = song.songName
        holder.artistName.text = song.artistName

        // Set a click listener to handle item click
        holder.itemView.setOnClickListener {
            onItemClick(song, position) // Pass the selected song and position to the click listener
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    private fun loadAlbumArt(imageView: ImageView, albumId: Long) {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart").buildUpon()
            .appendPath(albumId.toString()).build()

        Picasso.get()
            .load(albumArtUri)
            .placeholder(R.drawable.img_17) // Placeholder image
            .error(R.drawable.img_17) // Error image (if loading fails)
            .into(imageView)
    }
}

class MainActivity3 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        recyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (isReadStoragePermissionGranted()) {
            // Permission granted, display local songs
            val songList = getLocalSongs()
            val songAdapter = SongAdapter(songList) { song, position ->
                val intent = MainActivity4.newIntent(
                    this,
                    song.songName,
                    song.artistName,
                    song.filePath,
                    song.albumId,
                    songList.map { it.filePath }, // Pass the list of song paths to MainActivity4
                    position // Pass the selected position
                )
                startActivity(intent)
            }
            recyclerView.adapter = songAdapter
        } else {
            // Permission not granted, request it
            requestStoragePermission()
        }
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    private fun getLocalSongs(): List<SongModel> {
        val songList = ArrayList<SongModel>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val filePathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumId = it.getLong(albumIdColumn)
                val filePath = it.getString(filePathColumn)

                val song = SongModel(id, title ?: "", artist ?: "", albumId, filePath ?: "")
                songList.add(song)
            }
        }

        return songList
    }

    companion object {
        private const val READ_STORAGE_PERMISSION_REQUEST_CODE = 123
    }
}















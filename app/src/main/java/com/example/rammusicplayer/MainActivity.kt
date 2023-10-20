package com.example.rammusicplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Delay for 3 seconds (3000 milliseconds)
        val delayMillis = 3000L
        Handler(Looper.getMainLooper()).postDelayed({

            // Get the local music files
            val musicFiles = getLocalMusicFiles()

            // Pass the music files to the next activity
            val intent = Intent(this@MainActivity, MainActivity2::class.java)
            intent.putExtra("musicFiles", musicFiles)
            startActivity(intent)
            finish() // Optional, to close the current activity
        }, delayMillis)
    }

    private fun getLocalMusicFiles(): ArrayList<String> {
        val musicFiles = ArrayList<String>()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (it.moveToNext()) {
                val filePath = it.getString(dataIndex)
                musicFiles.add(filePath)
                Log.d("MusicPlayer", "File path: $filePath")
            }
        }

        return musicFiles
    }
}

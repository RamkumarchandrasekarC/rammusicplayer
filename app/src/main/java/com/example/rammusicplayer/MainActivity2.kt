package com.example.rammusicplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton



class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Find all the buttons by their IDs
        val searchButton = findViewById<MaterialButton>(R.id.searchButton)
        val recentlyPlayedButton = findViewById<MaterialButton>(R.id.recentlyPlayedButton)
        val recentlyAddedButton = findViewById<MaterialButton>(R.id.recentlyAddedButton)
        val folderListButton = findViewById<MaterialButton>(R.id.folderListButton)
        val bottomListsButton = findViewById<MaterialButton>(R.id.bottomListsButton)
        val bottomSongsButton = findViewById<MaterialButton>(R.id.bottomSongsButton)
        val bottomAlbumsButton = findViewById<MaterialButton>(R.id.bottomAlbumsButton)

        // Set click listeners for each button
        searchButton.setOnClickListener { goToMainActivity3() }
        recentlyPlayedButton.setOnClickListener { goToMainActivity3() }
        recentlyAddedButton.setOnClickListener { goToMainActivity3() }
        folderListButton.setOnClickListener { goToMainActivity3() }
        bottomListsButton.setOnClickListener { goToMainActivity3() }
        bottomSongsButton.setOnClickListener { goToMainActivity3() }
        bottomAlbumsButton.setOnClickListener { goToMainActivity3() }
    }

    // Method to start Activity 3
    private fun goToMainActivity3() {
        val intent = Intent(this@MainActivity2, MainActivity3::class.java)
        startActivity(intent)
    }

}

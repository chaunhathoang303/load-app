package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val intentNavigator = Intent(this, MainActivity::class.java)

        val fileName = intent.getStringExtra("fileName")
        val status = intent.getStringExtra("status")

        binding.detail.nameContent.text = fileName
        binding.detail.statusContent.text = status

        binding.detail.button.setOnClickListener {
            startActivity(intentNavigator)
        }
    }
}

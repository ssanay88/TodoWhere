package com.example.todowhere.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todowhere.R
import com.example.todowhere.databinding.ActivityMapPopUpBinding

class MapPopUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapPopUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPopUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
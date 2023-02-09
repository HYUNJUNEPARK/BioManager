package com.ex.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import com.module.biometricmanager.BioManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bioManager: BioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bioManager = BioManager(this)

        binding.testButton.setOnClickListener {
            bioManager.canAuthenticateByBioMetric()
        }
    }
}
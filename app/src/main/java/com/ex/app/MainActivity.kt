package com.ex.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import com.module.biometricmanager.BiometricManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        biometricManager = BiometricManager(this)

        binding.testButton.setOnClickListener {
            biometricManager.canAuthenticateByBioMetric()
        }
    }
}
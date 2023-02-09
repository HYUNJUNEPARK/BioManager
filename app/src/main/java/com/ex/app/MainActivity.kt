package com.ex.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import com.module.biometricmanager.BiometricManager
import com.module.biometricmanager.BiometricReturnType

//Databinding : https://lunadev.tistory.com/30

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this
        //binding.visibilityOfBiometric = null
        biometricManager = BiometricManager(this)
        initUI()
    }

    private fun initUI() {
        when(biometricManager.canAuthenticateByBioMetric()) {
            BiometricReturnType.FALSE -> {
                //지문 인증 관련 UI를 지운다.
            }
            else -> {
                //여기가 실행되는 것
                //binding.visibilityOfBiometric = true
            }
        }
    }

    fun onClickToUseBiometricManager() {
        when(biometricManager.canAuthenticateByBioMetric()) {
            BiometricReturnType.TRUE -> {
                biometricManager.showBiometricPrompt(this)
            }
            BiometricReturnType.EXCEPTION -> {
                //사용자에게 안내 후 UI를 비활성화한다.
                Toast.makeText(this, "일시적으로 생체 인증을 사용할 수 없거나 보안 취약점이 있습니다.", Toast.LENGTH_SHORT).show()
            }
            BiometricReturnType.UNENROLLED -> {
                biometricManager.showSecuritySettingDialog(this)
            }
            BiometricReturnType.FALSE -> {}
        }
    }
}
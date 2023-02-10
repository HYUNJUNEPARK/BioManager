package com.ex.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import com.module.biometricmanager.BiometricManager
import com.module.biometricmanager.BiometricReturnType

//Databinding : https://lunadev.tistory.com/30
/*
visibilityOfBiometric ? View.GONE : View.VISIBLE
visibilityOfBiometric -> null 일 경우 View.VISIBLE

 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this
        biometricManager = BiometricManager(this)

        initUI()
    }

    private fun initUI() {
        when(biometricManager.canAuthenticateByBioMetric()) {
            BiometricReturnType.FALSE -> {
                //지문 인증 관련 UI를 지운다.
                binding.isGoneBiometricUI = true
            }
            BiometricReturnType.EXCEPTION -> {
                Toast.makeText(this, "지문 인증을 사용할 수 없거나 보안 업데이트가 필요합니다.", Toast.LENGTH_SHORT).show()
                //버튼 비활성화
                binding.isUnableBiometric = true
                binding.isVisibleBiometricUI = true
            }
            else -> {}
        }
    }

    fun onClickToUseBiometricManager() {
        when(biometricManager.canAuthenticateByBioMetric()) {
            BiometricReturnType.TRUE -> {
                biometricManager.showBiometricPrompt(this)
            }
            BiometricReturnType.EXCEPTION -> {
                Toast.makeText(this, "일시적으로 생체 인증을 사용할 수 없거나 보안 업데이트가 필요합니다.", Toast.LENGTH_SHORT).show()
            }
            BiometricReturnType.UNENROLLED -> {
                biometricManager.showSecuritySettingDialog(this)
            }
            BiometricReturnType.FALSE -> {
                Toast.makeText(this, "기기에서 디바이스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
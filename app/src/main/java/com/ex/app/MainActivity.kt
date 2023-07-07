package com.ex.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this
        biometricManager = BiometricManager(this)

        Timber.d("AAAAAA")

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
            BiometricReturnType.UNENROLLED -> {
                biometricManager.showSecuritySettingDialog(this)
            }
            else -> {

            }
        }
    }
}
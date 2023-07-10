package com.ex.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ex.app.BiometricHelper.isPossibleToUseBiometric
import com.ex.app.BiometricHelper.showBiometricPrompt
import com.ex.app.BiometricHelper.showSecuritySettingDialog
import com.ex.app.EncryptionHelper.decryptString
import com.ex.app.EncryptionHelper.encryptString
import com.ex.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    //[1].BiometricReturnType.TRUE : 생체 인증 가능한 경우
    //[2].BiometricReturnType.FALSE : 디바이스에 적절한 인식 센서가 없는 경우
    //[3].BiometricReturnType.EMPTY : 생체 인식 정보가 등록되어 있지 않은 경우
    //[4].BiometricReturnType.EXCEPTION : 지문 인증을 일시적으로 사용할 수 없거나 보안 업데이트가 필요한 경우
    enum class BiometricReturnType {
        SUCCESS, FAIL, EXCEPTION, EMPTY
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this@MainActivity

        when(isPossibleToUseBiometric(this)) {
            BiometricReturnType.EXCEPTION -> { //버튼 비활성화
                Toast.makeText(this, "지문 인증을 사용할 수 없거나 보안 업데이트가 필요합니다.", Toast.LENGTH_SHORT).show()
                binding.isVisibleBiometricUI = true
            }
            else -> {}
        }
    }

    fun onTestButtonClicked() {
        when(isPossibleToUseBiometric(this)) {
            BiometricReturnType.SUCCESS -> {
                showBiometricPrompt(this)
            }
            BiometricReturnType.EMPTY -> {
                showSecuritySettingDialog(this)
            }
            else -> {}
        }
    }

    fun encrypted(password: String?) {
        val encryptString = encryptString(password)
        binding.mainTvEncrypt.text = encryptString
    }

    fun decrypted(password: String?) {
        val decryptString = decryptString(password)
        binding.mainTvDecrypt.text = decryptString
    }
}
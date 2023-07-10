package com.ex.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ex.app.BiometricHelper.isPossibleToUseBiometric
import com.ex.app.BiometricHelper.showBiometricPrompt
import com.ex.app.BiometricHelper.showSecuritySettingDialog
import com.ex.app.EncryptionHelper.decryptString
import com.ex.app.EncryptionHelper.encryptString
import com.ex.app.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    //[1].BiometricReturnType.TRUE : 생체 인증 가능한 경우
    //[2].BiometricReturnType.FALSE : 디바이스에 적절한 인식 센서가 없는 경우
    //[3].BiometricReturnType.EMPTY : 생체 인식 정보가 등록되어 있지 않은 경우
    //[4].BiometricReturnType.EXCEPTION : 지문 인증을 일시적으로 사용할 수 없거나 보안 업데이트가 필요한 경우
    enum class BiometricReturnType {
        SUCCESS, FAIL, EXCEPTION, EMPTY
    }

    enum class CipherMode {
        ENCRYPT, DECRYPT
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
        showCipherBiometricPrompt(this, CipherMode.ENCRYPT, password)
    }

    fun decrypted(password: String?) {
        showCipherBiometricPrompt(this, CipherMode.DECRYPT, password)
    }

    private fun showCipherBiometricPrompt(activity: AppCompatActivity, mode: CipherMode, password: String?) {
        val title = when(mode) {
            CipherMode.ENCRYPT -> "암호화 생체인증"
            CipherMode.DECRYPT -> "복호화 생체인증"
        }

        val promptUi = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setDescription("생체정보로 인증해 주세요")
            setNegativeButtonText("취소")
            setConfirmationRequired(false)
        }.build()

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) { //지문 인식 ERROR
                super.onAuthenticationError(errCode, errString)
                Timber.e("errCode is $errCode and errString is: $errString")
                BiometricPrompt.ERROR_NO_BIOMETRICS
                when(errCode) {
                    BiometricPrompt.ERROR_CANCELED -> {
                        //errCode is 5 and errString is: 지문 인식 작업이 취소되었습니다.
                    }
                    BiometricPrompt.ERROR_LOCKOUT -> {
                        //시도 횟수가 너무 많습니다. 나중에 다시 시도하세요. -> 30 초 블럭!
                    }
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT-> {
                        //시도 횟수가 너무 많습니다. 지문 센서가 사용 중지되었습니다. -> 제법 오랜 시간 블럭됨
                    }
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        // errCode is 10 and errString is: 사용자가 지문 인식 작업을 취소했습니다.
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        //등록된 지문이 없는 에러 / 얼굴 인식 잠금 해제를 설정하지 않았습니다.
                        //errCode is 11 and errString is: 얼굴 인식 잠금 해제를 설정하지 않았습니다.
                        Toast.makeText(activity.applicationContext, "$errString", Toast.LENGTH_SHORT).show()
                        showSecuritySettingDialog(activity)
                    }
                    else -> {
                        Timber.e("errCode Else Block: $errCode")
                    }
                }

            }

            override fun onAuthenticationFailed() { //"지문 인식 실패"
                super.onAuthenticationFailed()
                Timber.d("User biometric rejected.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { //"지문 인식 성공"
                super.onAuthenticationSucceeded(result)
                Toast.makeText(activity.applicationContext, "생체인식 성공!!!", Toast.LENGTH_SHORT).show()
                Timber.d("Authentication was successful")
                when(mode) {
                    CipherMode.DECRYPT -> {
                        val decryptString = decryptString(password)
                        binding.mainTvDecrypt.text = decryptString
                    }
                    CipherMode.ENCRYPT -> {
                        val encryptString = encryptString(password)
                        binding.mainTvEncrypt.text = encryptString
                    }
                }
            }
        }
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptUi)
    }
}
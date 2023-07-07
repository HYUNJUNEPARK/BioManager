package com.ex.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ex.app.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    /**
     * [1].BiometricReturnType.TRUE : 생체 인증 가능한 경우
     * [2].BiometricReturnType.FALSE : 디바이스에 적절한 인식 센서가 없는 경우
     * [3].BiometricReturnType.EMPTY : 생체 인식 정보가 등록되어 있지 않은 경우
     * [4].BiometricReturnType.EXCEPTION : 지문 인증을 일시적으로 사용할 수 없거나 보안 업데이트가 필요한 경우
     */
    private enum class BiometricReturnType {
        SUCCESS, FAIL, EXCEPTION, EMPTY
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this

        checkDeviceAndUpdateUi()
    }

    private fun checkDeviceAndUpdateUi() {
        when(isPossibleToUseBiometric()) {
            BiometricReturnType.FAIL -> { //지문 인증 관련 UI를 지운다.
                binding.isPossibleBiometric = false
            }
            BiometricReturnType.EXCEPTION -> { //버튼 비활성화
                Toast.makeText(this, "지문 인증을 사용할 수 없거나 보안 업데이트가 필요합니다.", Toast.LENGTH_SHORT).show()
                binding.isUnableBiometric = true
                binding.isVisibleBiometricUI = true
            }
            else -> {}
        }
    }

    //생체 인증이 가능한지 확인한다.
    private fun isPossibleToUseBiometric(): BiometricReturnType {
        val canAuthenticate = androidx.biometric.BiometricManager.from(this).canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        when (canAuthenticate) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("BIOMETRIC_SUCCESS")
                return BiometricReturnType.SUCCESS
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> { //생체 인식 정보가 등록되어 있지 않은 경우
                Timber.d("BIOMETRIC_ERROR_NONE_ENROLLED")
                return BiometricReturnType.EMPTY
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> { //디바이스에 적절한 센서가 없는 경우
                Timber.d("BIOMETRIC_ERROR_NO_HARDWARE")
                return BiometricReturnType.FAIL
            }
            else -> { //지문 인증을 사용할 수 없거나 보안 업데이트가 필요한 경우
                Timber.d("BIOMETRIC_ERROR_HW_UNAVAILABLE or BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
                return BiometricReturnType.EXCEPTION
            }
        }
    }

    //지문 인식이 가능한 경우, 지문 인식 프롬프트를 띄운다.
    private fun showBiometricPrompt(activity: AppCompatActivity) {
        val promptUi = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(getString(R.string.prompt_title))
            setSubtitle(getString(R.string.prompt_subtitle))
            setDescription(getString(R.string.prompt_description))
            setNegativeButtonText(getString(R.string.prompt_negative_button))
            setConfirmationRequired(false)
        }.build()

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) { //지문 인식 ERROR
                super.onAuthenticationError(errCode, errString)
                Timber.e("errCode is $errCode and errString is: $errString")
                if (errCode == 11) { //등록된 지문이 없는 에러
                    showSecuritySettingDialog(activity)
                }
            }
            override fun onAuthenticationFailed() { //"지문 인식 실패"
                super.onAuthenticationFailed()
                Timber.d("User biometric rejected.")
            }
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { //"지문 인식 성공"
                super.onAuthenticationSucceeded(result)
                Timber.d("Authentication was successful")
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptUi)
    }

    //지문이 등록되어 있지 않은 경우 등록 설정창을 띄운다.
    private fun showSecuritySettingDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder
            .setTitle("나의 앱")
            .setMessage("지문 등록이 필요합니다.\n지문등록 설정화면으로 이동하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                goBiometricEnrollActivity(context)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
        dialogBuilder.show()
    }

    //지문 등록 화면으로 이동한다.(API 30 부터 사용)
    private fun goBiometricEnrollActivity(context: Context) {
        if (Build.VERSION.SDK_INT > 29) {
            val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
            context.startActivity(intent)
        }
    }

    fun onTestButtonClicked() {
        when(isPossibleToUseBiometric()) {
            BiometricReturnType.SUCCESS -> showBiometricPrompt(this)
            BiometricReturnType.EMPTY -> showSecuritySettingDialog(this)
            else -> {}
        }
    }
}
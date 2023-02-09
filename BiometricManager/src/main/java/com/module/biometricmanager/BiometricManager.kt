package com.module.biometricmanager

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

//TODO 이름을 뭐라고 해야할지 모르겠음 ...
enum class CanUseBiometric {
    TRUE,
    FALSE,
    EXCEPTION
}

/**
 * BiometricPrompt API Lev 28
 * BiometricManager API Lev 29
 */
class BiometricManager(private val activity: AppCompatActivity) {
    /**
     * 생체 인증이 가능한지 확인한다.
     */
    fun canAuthenticateByBioMetric() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val canAuthenticate = BiometricManager.from(activity).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_SUCCESS -> { //생체 인증 가능한 경우
                    LogUtil.logD("App can authenticate using biometrics.")
                    showBiometricPrompt(activity)
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> { //디바이스에서 생체 인증을 지원하지 않는 경우
                    LogUtil.logD("No biometric features available on this device.")
                }


                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> { //현재 생체 인증을 사용할 수 없는 경우
                    LogUtil.logD("Biometric features are currently unavailable. Try again later")
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> { //생체 인식 정보가 등록되어 있지 않은 경우
                    LogUtil.logD("Prompts the user to create credentials that your app accepts.")
                    showSecuritySettingDialog(activity)
                }
            }
        } else { //API 버전 미만

        }
    }

    /**
     * 지문 인식이 가능한 경우 지문 인식 프롬프트를 띄운다.
     */
    private fun showBiometricPrompt(activity: AppCompatActivity) {
        val promptUiInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Sample App Authentication")
            setSubtitle("Please login to get access")
            setDescription("Sample App is using Android biometric authentication")
            setConfirmationRequired(false)
            setNegativeButtonText("close")
        }.build()

        //지문 인식 프롬프트
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) { //지문 인식 ERROR
                super.onAuthenticationError(errCode, errString)
                LogUtil.logD("errCode is $errCode and errString is: $errString")
            }

            override fun onAuthenticationFailed() { //"지문 인식 실패"
                super.onAuthenticationFailed()
                LogUtil.logD("User biometric rejected.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { //"지문 인식 성공"
                super.onAuthenticationSucceeded(result)
                LogUtil.logD("Authentication was successful")
            }
        }
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            callback
        )
        biometricPrompt.authenticate(promptUiInfo)
    }

    /**
     * 지문이 등록되어 있지 않은 경우 등록 설정창을 띄운다.
     */
    private fun showSecuritySettingDialog(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) { ////ACTION_BIOMETRIC_ENROLL 지원
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder
                .setTitle("나의 앱")
                .setMessage("지문 등록이 필요합니다. 지문등록 설정화면으로 이동하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply { //API30
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                  android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    context.startActivity(intent)
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.cancel()
                }
            dialogBuilder.show()
        } else { //ACTION_BIOMETRIC_ENROLL 지원 안하는 버전
            LogUtil.logD("ACTION_BIOMETRIC_ENROLL 지원 안하는 버전")
        }
    }
}



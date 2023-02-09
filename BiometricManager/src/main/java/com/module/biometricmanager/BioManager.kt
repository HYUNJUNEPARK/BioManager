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
import com.module.biometricmanager.util.LogUtil

class BioManager(private val activity: AppCompatActivity) {
    /**
     * 생체 인증이 가능한지 확인한다.
     */
    fun canAuthenticateByBioMetric() {
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        when (canAuthenticate) {
            //생체 인증 가능
            BiometricManager.BIOMETRIC_SUCCESS -> {
                LogUtil.logD("App can authenticate using biometrics.")
                showBiometricPrompt(activity)
            }

            //기기에서 생체 인증을 지원하지 않는 경우
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                LogUtil.logD("No biometric features available on this device.")
            }

            //현재 생체 인증을 사용할 수 없는 경우
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                LogUtil.logD("Biometric features are currently unavailable.")
            }

            //생체 인식 정보가 등록되어 있지 않은 경우
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                LogUtil.logD("Prompts the user to create credentials that your app accepts.")
                showSecuritySettingDialog(activity)
            }
        }
    }


    fun showBiometricPrompt(activity: AppCompatActivity) {
        val biometricPrompt = createBiometricPrompt(activity)
        val promptInfo = createPromptInfo()

        biometricPrompt.authenticate(promptInfo)
    }


    fun createBiometricPrompt(activity: AppCompatActivity): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            //지문 인식 ERROR
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                LogUtil.logD("errCode is $errCode and errString is: $errString")
            }

            //"지문 인식 실패"
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                LogUtil.logD("User biometric rejected.")
            }

            //"지문 인식 성공"
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                LogUtil.logD("Authentication was successful")
            }
        }

        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * 지문 인식 프롬프트
     */
    fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Sample App Authentication")
            setSubtitle("Please login to get access")
            setDescription("Sample App is using Android biometric authentication")
            setConfirmationRequired(false)
            setNegativeButtonText("close")
        }.build()

        return promptInfo
    }

    /**
     * 지문등록 설정창을 띄워준다.
     */
    private fun showSecuritySettingDialog(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) { ////ACTION_BIOMETRIC_ENROLL 지원
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder
                .setTitle("나의앱")
                .setMessage("지문 등록이 필요합니다. 지문등록 설정화면으로 이동하시겠습니까?")
                .setPositiveButton("확인") { dialog, which ->
                    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply { //API30
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                  android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    context.startActivity(intent)
                }
                .setNegativeButton("취소") { dialog, which -> dialog.cancel() }
            dialogBuilder.show()
        } else { //ACTION_BIOMETRIC_ENROLL 지원 안하는 버전
            LogUtil.logD("ACTION_BIOMETRIC_ENROLL 지원안하는 버전")
        }
    }


}



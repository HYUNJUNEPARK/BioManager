package com.module.biometricmanager

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import com.module.biometricmanager.util.LogUtil

class BioManager(private val context: Context) {
    /**
     * 생체 인증이 가능한지 확인한다.
     */
    fun canAuthenticateByBioMetric() {
        val canAuthenticate = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        when (canAuthenticate) {
            //생체 인증 가능
            BiometricManager.BIOMETRIC_SUCCESS -> {
                LogUtil.logD("App can authenticate using biometrics.")
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
                showSecuritySettingDialog(context)
            }
        }
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



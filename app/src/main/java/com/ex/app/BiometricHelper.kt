package com.ex.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import timber.log.Timber

object BiometricHelper {
    private var blockMillis = 30000L
    private var blockTime = 0L
    private var isBiometricBlocked: Boolean = false

    //생체 인증이 가능한지 확인한다.
    fun isPossibleToUseBiometric(context: Context): MainActivity.BiometricReturnType {
        val canAuthenticate = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("BIOMETRIC_SUCCESS")
                return MainActivity.BiometricReturnType.SUCCESS
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> { //생체 인식 정보가 등록되어 있지 않은 경우
                Timber.d("BIOMETRIC_ERROR_NONE_ENROLLED")
                return MainActivity.BiometricReturnType.EMPTY
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> { //디바이스에 적절한 센서가 없는 경우
                Timber.d("BIOMETRIC_ERROR_NO_HARDWARE")
                return MainActivity.BiometricReturnType.FAIL
            }
            else -> { //지문 인증을 사용할 수 없거나 보안 업데이트가 필요한 경우
                Timber.d("BIOMETRIC_ERROR_HW_UNAVAILABLE or BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
                return MainActivity.BiometricReturnType.EXCEPTION
            }
        }
    }

    //지문 인식이 가능한 경우, 지문 인식 프롬프트를 띄운다.
    fun showBiometricPrompt(activity: AppCompatActivity) {
        if (isBiometricBlocked) {
            Timber.d("임시 블럭 상태 // 블럭시간 $blockTime //블럭 해제 시간 ${blockTime + blockMillis} //현재 시간 : ${System.currentTimeMillis()}")
            if (System.currentTimeMillis() < (blockTime + blockMillis)) { //블럭 상태
                Toast.makeText(activity.applicationContext, "시도 횟수가 너무 많습니다. 나중에 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                return
            } else {
                isBiometricBlocked = true
                Timber.d("블럭 해제!!!!")
            }
        }

        val promptUi = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.applicationContext.getString(R.string.prompt_title))
            setSubtitle(activity.applicationContext.getString(R.string.prompt_subtitle))
            setDescription(activity.applicationContext.getString(R.string.prompt_description))
            setNegativeButtonText(activity.applicationContext.getString(R.string.prompt_negative_button))
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
                        blockTime = System.currentTimeMillis()
                        isBiometricBlocked = true //생체 인식 30초 임시 블럭
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
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptUi)
    }

    //생체 인식 정보가 등록되어 있지 않거나, 센서 사용이 중지된 경우.
    fun showSecuritySettingDialog(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder
            .setTitle("알림")
            .setMessage("생체 인식 정보가 등록되어 있지 않거나, 인식 센서 사용이 중지되었습니다. 등록 화면으로 이동하시겠습니까?")
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
}
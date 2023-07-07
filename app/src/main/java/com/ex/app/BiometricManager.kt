package com.ex.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * compileSdk 33
 * minSdk 28
 * targetSdk 31
 */
class BiometricManager(private val activity: AppCompatActivity) {
    /**
     * 생체 인증이 가능한지 확인한다.
     *
     * @return BiometricReturnType
     * [1].BiometricReturnType.TRUE : 생체 인증 가능한 경우 -> showBiometricPrompt()
     * [2].BiometricReturnType.FALSE : 디바이스에 적절한 인식 센서가 없는 경우 -> 지문 인증 관련 UI를 모두 가린다.
     * [3].BiometricReturnType.UNENROLLED : 생체 인식 정보가 등록되어 있지 않은 경우 -> 사용자에게 안내 후 설정창을 띄워 지문 등록을 유도한다.() -> showSecuritySettingDialog()
     * [4].BiometricReturnType.EXCEPTION : 지문 인증을 일시적으로 사용할 수 없거나 보안 업데이트가 필요한 경우 -> 사용자에게 안내하고 기능을 비활성화한다.
     */
    fun canAuthenticateByBioMetric(): BiometricReturnType {
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                LogUtil.logD("BIOMETRIC_SUCCESS")
                return BiometricReturnType.TRUE
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                LogUtil.logD("BIOMETRIC_ERROR_NONE_ENROLLED")
                return BiometricReturnType.UNENROLLED
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> { //디바이스에 적절한 센서가 없는 경우
                LogUtil.logD("BIOMETRIC_ERROR_NO_HARDWARE")
                return BiometricReturnType.FALSE
            }

            else -> { //지문 인증을 사용할 수 없거나 보안 업데이트가 필요한 경우
                LogUtil.logD("BIOMETRIC_ERROR_HW_UNAVAILABLE or BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
                return BiometricReturnType.EXCEPTION
            }
        }
    }

    /**
     * 지문 인식이 가능한 경우 지문 인식 프롬프트를 띄운다.
     * canAuthenticateByBioMetric() 의 반환값이 BiometricReturnType.TRUE 인 경우 사용한다.
     *
     * @param activity
     */
    fun showBiometricPrompt(activity: AppCompatActivity) {
        val promptUiInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(this@BiometricManager.activity.getString(R.string.prompt_title))
            setSubtitle(this@BiometricManager.activity.getString(R.string.prompt_subtitle))
            setDescription(this@BiometricManager.activity.getString(R.string.prompt_description))
            setNegativeButtonText(this@BiometricManager.activity.getString(R.string.prompt_negative_button))
            setConfirmationRequired(false)
        }.build()

        //지문 인식 프롬프트
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) { //지문 인식 ERROR
                super.onAuthenticationError(errCode, errString)
                LogUtil.logD("errCode is $errCode and errString is: $errString")

                if (errCode == 11) { //등록된 지문이 없는 에러
                    showSecuritySettingDialog(activity)
                }
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
     * canAuthenticateByBioMetric() 의 반환값이 BiometricReturnType.UNENROLLED 인 경우 사용한다.
     *
     * @param context
     */
    fun showSecuritySettingDialog(context: Context) {
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

    /**
     * 지문 등록 화면으로 이동한다.
     * 앱에서 커스텀된 다이얼로그에서 필요한 경우 사용한다.
     * API 30 부터 사용 가능
     *
     * @param context
     */
    fun goBiometricEnrollActivity(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) { //Q: API 29
            val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
            context.startActivity(intent)
        }
    }
}



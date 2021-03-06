package dku.gyeongsotone.gulging.campusplogging.ui.univcertification

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dku.gyeongsotone.gulging.campusplogging.data.repository.CamploRepository
import dku.gyeongsotone.gulging.campusplogging.data.repository.Result
import dku.gyeongsotone.gulging.campusplogging.utils.Constant
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpString
import kotlinx.coroutines.launch

class MailAuthViewModel : ViewModel() {
    companion object {
        private val TAG = this::class.java.name
    }

    private val repository = CamploRepository

    // 사용자가 입력한 학번/인증번호
    val studentId = ObservableField<String>()
    val verificationCode = ObservableField<String>()

    // 학번/인증코드 입력 상태
    val studentIdStatus = ObservableField<StudentIdStatus>(StudentIdStatus.EMPTY)
    val verificationCodeStatus =
        ObservableField<VerificationCodeStatus>(VerificationCodeStatus.BEFORE_SEND)

    // keyboard hide 여부
    private val _keyboardHide = MutableLiveData<Boolean>()
    val keyboardHide: LiveData<Boolean> = _keyboardHide

    // 토스트 메시지
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg


    /** 인증번호 전송 */
    fun onClickSendVerificationCodeBtn() {
        viewModelScope.launch {
            val response = repository.sendMailAuth(studentId.get()!!)

            // 오류가 발생했을 경우, 에러 메시지 띄운 후 리턴
            if (response is Result.Error) { // error
                _toastMsg.value = response.message
                return@launch
            }

            _toastMsg.value = "인증번호가 전송되었습니다."
            studentIdStatus.set(StudentIdStatus.WAIT)
            verificationCodeStatus.set(VerificationCodeStatus.WAIT_EMPTY)
        }
    }

    /** 인증하기 */
    fun onClickVerifyCodeBtn() {
        _keyboardHide.value = true

        viewModelScope.launch {
            val token = getSpString(Constant.SP_TOKEN)!!
            val response = repository.verifyMailAuth(token, verificationCode.get()!!)

            // 오류가 발생했을 경우, 에러 메시지 띄운 후 리턴
            if (response is Result.Error) { // error
                _toastMsg.value = response.message
                return@launch
            }

            if (verificationCodeStatus.get() == VerificationCodeStatus.WAIT) {
                _toastMsg.value = "인증이 완료되었습니다."
                studentIdStatus.set(StudentIdStatus.SUCCESS)
                verificationCodeStatus.set(VerificationCodeStatus.SUCCESS)
            }
        }
    }

    /** text 입력 후에 조건에 따라 입력 상태 변경 */
    fun onStudentIdTextChanged(s: CharSequence) {
        when {
            verificationCodeStatus.get() == VerificationCodeStatus.BEFORE_SEND && s.length == 8 ->
                studentIdStatus.set(StudentIdStatus.BEFORE_SEND)

            verificationCodeStatus.get() == VerificationCodeStatus.WAIT && s.length == 8 ->
                studentIdStatus.set(StudentIdStatus.WAIT)

            verificationCodeStatus.get() == VerificationCodeStatus.WAIT && s.length != 8 ->
                studentIdStatus.set(StudentIdStatus.WAIT_EMPTY)

            else -> studentIdStatus.set(StudentIdStatus.EMPTY)
        }
    }

    fun onVerificationCodeTextChanged(s: CharSequence) {
        when {
            verificationCodeStatus.get() == VerificationCodeStatus.WAIT_EMPTY && s.length == 6 ->
                verificationCodeStatus.set(VerificationCodeStatus.WAIT)

            verificationCodeStatus.get() == VerificationCodeStatus.WAIT && s.length != 6 ->
                verificationCodeStatus.set(VerificationCodeStatus.WAIT_EMPTY)
        }
    }

}

enum class StudentIdStatus {
    EMPTY, BEFORE_SEND, WAIT, WAIT_EMPTY, SUCCESS
}

enum class VerificationCodeStatus {
    BEFORE_SEND, WAIT_EMPTY, WAIT, SUCCESS
}
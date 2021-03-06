package dku.gyeongsotone.gulging.campusplogging.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dku.gyeongsotone.gulging.campusplogging.APP
import dku.gyeongsotone.gulging.campusplogging.R
import dku.gyeongsotone.gulging.campusplogging.data.local.model.UnivCertStatus
import dku.gyeongsotone.gulging.campusplogging.databinding.FragmentSignInBinding
import dku.gyeongsotone.gulging.campusplogging.ui.custom.LoadingDialog
import dku.gyeongsotone.gulging.campusplogging.ui.main.MainActivity
import dku.gyeongsotone.gulging.campusplogging.ui.univcertification.UnivCertificationActivity
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_TOKEN
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpString
import dku.gyeongsotone.gulging.campusplogging.utils.getApplication
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.name
    }

    private lateinit var application: APP
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()
    private val uiScope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        init(inflater, container)
        checkAccessToken()

        return binding.root
    }

    /**
     * 초기 설정
     */
    private fun init(inflater: LayoutInflater, container: ViewGroup?) {
        // binding 설정
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_in,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        application = getApplication(requireActivity())

        setSpannableText()
        setClickListener()  // 클릭 리스너 설정
        setObserver()  // observer 설정
    }

    /**
     * spannable text 설정
     */
    private fun setSpannableText() {
        val signUpBtnSpannable = SpannableStringBuilder("혹시 처음 오셨나요? 회원가입하러 가기")
        signUpBtnSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue_link)),
            12,
            21,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        signUpBtnSpannable.setSpan(UnderlineSpan(), 0, 21, 0)
        binding.btnSignUp.text = signUpBtnSpannable
    }


    /**
     * 액세스 토큰 존재하면 바로 메인화면으로 이동
     */
    private fun checkAccessToken() {

        when (val token = getSpString(SP_TOKEN)) {
            null, "" -> binding.layoutSplash.root.isGone = true
            else -> viewModel.tokenLogin(token)
        }
    }


    /**
     * 클릭 리스너 설정
     */
    private fun setClickListener() {
        binding.btnSignUp.setOnClickListener { onSignUpBtnClick() }
        binding.btnSignIn.setOnClickListener { onSignInBtnClick() }
    }


    /**
     * 로그인 버튼 클릭 시, 로그인 요청
     */
    private fun onSignInBtnClick() {
        uiScope.launch {
            LoadingDialog.showWhileDoJob(requireContext(), viewModel.signIn(), "로그인 중입니다")
        }
    }


    /**
     * 회원가입 버튼 클릭 시, 회원가입 프래그먼트로 이동
     */
    private fun onSignUpBtnClick() {
        findNavController().navigate(
            SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
        )
    }

    /**
     * observer 설정
     */
    private fun setObserver() {
        setLoginResultObserver()
        setToastMsgObserver()
    }

    /**
     * 로그인 성공 시 유저를 application에 넣고 메인화면으로 이동
     */
    private fun setLoginResultObserver() {
        viewModel.signInResult.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "login result: $result")
            if (result == SignInStatus.SUCCESS) {
                application.user = viewModel.user
                navigateToNextStep()
            }
        }
    }

    /**
     * 인증 여부에 따라 다음 단계로 이동
     */
    private fun navigateToNextStep() {
        // 학교 인증 되어있으면 메인으로 이동
        if (application.user!!.univCertStatus == UnivCertStatus.DONE) {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        } else {  // 학교 인증 안 되어있으면 인증 화면으로 이동
            val intent = Intent(context, UnivCertificationActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    /**
     * 토스트 메시지 Observer 설정
     */
    private fun setToastMsgObserver() {
        viewModel.toastMsg.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
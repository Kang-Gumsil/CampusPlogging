package dku.gyeongsotone.gulging.campusplogging.data.repository

import android.util.Log
import dku.gyeongsotone.gulging.campusplogging.data.Result
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Plogging
import dku.gyeongsotone.gulging.campusplogging.data.local.model.RankingInfo
import dku.gyeongsotone.gulging.campusplogging.data.local.model.User
import dku.gyeongsotone.gulging.campusplogging.data.network.*
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_TOKEN
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpString
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.setSpString

object CamploRepository {
    private val TAG = this::class.java.name
    private val client = CamploApi.client

    /**
     * 회원 가입
     *
     * @return [String] error message (null if success)
     * @return [User] user(null if fail)
     */
    suspend fun signUp(userId: String, password: String): Result<User> {
        val request = SignUpRequest(userId, password)
        val response = client.signUp(request)
        Log.d(TAG, "signUp response: \n${response}")
        Log.d(TAG, "signUp body: \n${response.body()}")

        return when (response.body()?.success) {
            true -> {
                setSpString(SP_TOKEN, response.body()!!.token!!)
                Result.Success(response.body()!!.user!!.toUser())
            }
            false -> Result.Error(response.body()!!.description ?: "로그인에 실패하였습니다.")
            else -> Result.Error(response.message())
        }
    }

    /**
     * 로그인
     */
    suspend fun signIn(userId: String, password: String): Result<User> {
        val request = SignInRequest(userId, password)
        val response = client.signIn(request)
        Log.d(TAG, "signIn response: \n${response}")
        Log.d(TAG, "signIn body: \n${response.body()}")

        return when (response.body()?.success) {
            true -> {
                setSpString(SP_TOKEN, response.body()!!.token!!)
                Result.Success(response.body()!!.user!!.toUser())
            }
            false -> Result.Error(response.body()!!.description ?: "로그인에 실패하였습니다.")
            else -> Result.Error(response.message())
        }
    }

    /**
     * 아이디 중복 체크

     */
    suspend fun idDupCheck(userId: String): Result<Unit> {
        val response = client.idDupCheck(userId)
        Log.d(TAG, "idDupCheck response: \n${response}")
        Log.d(TAG, "idDupCheck body: \n${response.body()}")

        return when (response.body()?.isExisting?.let { !it }) {
            true -> Result.Success(Unit)
            false -> Result.Error("이미 존재하는 아이디입니다.")
            null -> Result.Error(response.message())
        }
    }

    /**
     * 토큰으로 로그인
     */
    suspend fun tokenLogin(token: String): Result<User> {
        val response = client.tokenLogin(token)
        Log.d(TAG, "tokenLogin response: \n${response}")
        Log.d(TAG, "tokenLogin body: \n${response.body()}")

        return when (response.body()?.success) {
            true -> Result.Success(response.body()!!.user!!.toUser())
            false -> Result.Error(response.body()!!.description ?: "로그인에 실패하였습니다.")
            else -> Result.Error(response.message())
        }
    }

    /**
     * 재학생 인증 메일 보내기
     */
    suspend fun sendMailAuth(studentId: String): Result<Unit> {
        val token = getSpString(SP_TOKEN)!!
        val request = SendMailAuthRequest(token, studentId)
        val response = client.sendMailAuth(request)
        Log.d(TAG, "sendMailAuth response: \n${response}")
        Log.d(TAG, "sendMailAuth body: \n${response.body()}")

        return when (response.body()?.success) {
            true -> Result.Success(Unit)
            false -> Result.Error(response.body()!!.description ?: "인증 메일 전송이 실패하였습니다")
            null -> Result.Error(response.message())
        }
    }

    /**
     * 재학생 인증 코드 확인
     */
    suspend fun verifyMailAuth(token: String, verificationCode: String): Result<Unit> {
        val request = VerifyMailAuthRequest(token, verificationCode)
        val response = client.verifyMailAuth(request)
        Log.d(TAG, "verifyMailAuth response: \n${response}")
        Log.d(TAG, "verifyMailAuth body: \n${response.body()}")

        return when (response.body()?.success) {
            true -> Result.Success(Unit)
            false -> Result.Error(response.body()!!.description ?: "인증 코드 확인이 실패하였습니다")
            null -> Result.Error(response.message())
        }
    }

    /**
     * 랭킹 가져오기
     */
    suspend fun getRanking(token: String): Result<RankingInfo> {
        val response = client.getRanking(token)

        return when (response.body()?.success) {
            true -> Result.Success(response.body()!!.toRankingInfo())
            false -> Result.Error(response.body()!!.description)
            else -> Result.Error(response.message())
        }

    }

    /**
     * 플로깅 기록들 복원하기
     */
    suspend fun restorePloggingData(token: String): Result<List<Plogging>> {
        val response = client.restorePloggingData(token)

        return when (response.isSuccessful) {
            true -> Result.Success(response.body()!!.history.map { it.toPlogging() })
            false -> Result.Error(response.message())
        }
    }

    /**
     * 플로깅 기록 백업하기
     */
    suspend fun backUpPlogging(token: String, plogging: Plogging): Result<Unit> {
        val response = client.backUpPlogging(
            plogging.toBackUpRequestMap(token),
            plogging.picture.toMultipartBody()
        )

        return when (response.isSuccessful) {
            true -> Result.Success(Unit)
            false -> Result.Error(response.message())
        }
    }

    /**
     * 백업된 플로깅 기록 삭제하기
     */
    suspend fun deletePlogging(token: String, id: Int): Result<Unit> {
        val response = client.deletePlogging(token, id)

        return when (response.isSuccessful) {
            true -> Result.Success(Unit)
            false -> Result.Error(response.message())
        }
    }
}
package dku.gyeongsotone.gulging.campusplogging.ui.main.plogging

import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Plogging
import dku.gyeongsotone.gulging.campusplogging.data.repository.CamploRepository
import dku.gyeongsotone.gulging.campusplogging.data.repository.PloggingRepository
import dku.gyeongsotone.gulging.campusplogging.data.repository.Result
import dku.gyeongsotone.gulging.campusplogging.utils.Constant
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.CHALLENGE_LIST
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_CHALLENGE_ID
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_LEVEL
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_PROGRESS
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_REMAIN_DISTANCE
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_TOTAL_DISTANCE
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.UNIV_DISTANCE
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpDouble
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpInt
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.setSpDouble
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.setSpInt
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

class MainPloggingViewModel : ViewModel() {
    private val camploRepository = CamploRepository
    private val ploggingRepository = PloggingRepository

    val level = ObservableInt(getSpInt(SP_LEVEL))  // n학교
    val remainDistance = ObservableDouble(getSpDouble(SP_REMAIN_DISTANCE)) // km
    val progress = ObservableInt(getSpInt(SP_PROGRESS)) // 진행 비율

    val challengeName = ObservableField<String>()  // 진행중인 챌린지 제목
    val challengeContent = ObservableField<String>()  // 진행중인 챌린지 내용

    // 토스트 메시지
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    init {
        // 기존에 저장된 값으로 초기값 설정
        val challenge = CHALLENGE_LIST[getSpInt(SP_CHALLENGE_ID)]
        challengeName.set(challenge.name)
        challengeContent.set(challenge.getFullContent())
    }

    /**
     * 화면의 데이터 갱신
     */
    fun updateData() = viewModelScope.launch {
        val updateChallengeJob = updateChallenge()
        val updatePloggingProgressJob = updatePloggingProgress()

        joinAll(updateChallengeJob, updatePloggingProgressJob)
    }

    /**
     * 챌린지 갱신
     */
    private fun updateChallenge() = viewModelScope.launch {
        val time = ploggingRepository.getTotalTime()
        val level = floor(ploggingRepository.getTotalDistance() / UNIV_DISTANCE).toInt()
        val trashKind = ploggingRepository.getTrashKind()
        val totalTrash = ploggingRepository.getTotalTrash()

        for (challenge in CHALLENGE_LIST) {
            if (!challenge.isAchieved(time, level, trashKind, totalTrash)) {
                setSpInt(SP_CHALLENGE_ID, challenge.id)
                challengeName.set(challenge.name)
                challengeContent.set(challenge.getFullContent())
                break
            }
        }
    }


    /**
     * 거리 관련 플로깅 진행도 갱신
     */
    private fun updatePloggingProgress() = viewModelScope.launch {
        val totalDistance: Double = ploggingRepository.getTotalDistance()
        val curDistance: Double = totalDistance % UNIV_DISTANCE // 현재 레벨에서의 진행된 거리
        val curLevel: Int = floor(totalDistance / UNIV_DISTANCE).toInt()
        val curProgress: Int = (curDistance / UNIV_DISTANCE * 100).roundToInt() % 100
        val curRemainDistance: Double = UNIV_DISTANCE - curDistance

        level.set(curLevel)
        progress.set(curProgress)
        remainDistance.set(curRemainDistance)

        setSpDouble(SP_TOTAL_DISTANCE, totalDistance)
        setSpInt(SP_LEVEL, curLevel)
        setSpInt(SP_PROGRESS, curProgress)
        setSpDouble(SP_REMAIN_DISTANCE, curRemainDistance)
    }

    /**
     * 서버에서 플로깅 데이터 가져와서 내장DB에 넣기
     */
    fun restorePloggingData() = viewModelScope.launch {
        val token = PreferenceUtil.getSpString(Constant.SP_TOKEN)!!
        val response = camploRepository.restorePloggingData(token)

        // 오류가 발생했을 경우, 에러 메시지 띄운 후 리턴
        if (response is Error) {
            _toastMsg.value = response.message
            return@launch
        }

        val data = (response as Result.Success<List<Plogging>>).data
        val ploggingRepository = PloggingRepository

        // 플로깅 기록 하나씩 내장DB에 넣기
        data.forEach { ploggingRepository.insert(it) }
    }
}

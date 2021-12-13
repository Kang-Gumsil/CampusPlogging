package dku.gyeongsotone.gulging.campusplogging.utils.bindingadapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dku.gyeongsotone.gulging.campusplogging.R
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Challenge
import dku.gyeongsotone.gulging.campusplogging.data.local.model.ChallengeStatus
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Plogging
import dku.gyeongsotone.gulging.campusplogging.data.local.model.RankingUser
import dku.gyeongsotone.gulging.campusplogging.ui.main.challenge.ChallengeAdapter
import dku.gyeongsotone.gulging.campusplogging.ui.main.history.PloggingHistoryAdapter
import dku.gyeongsotone.gulging.campusplogging.ui.main.ranking.RankingAdapter

/**
 * MainActivity와 관련된 binding adpater
 *
 */

private val TAG = "MainBindingAdapter"

@SuppressLint("NotifyDataSetChanged")
@BindingAdapter("ploggingHistory")
fun RecyclerView.setPloggingHistory(items: List<Plogging>?) {
    if (adapter == null) {
        adapter = PloggingHistoryAdapter().apply {
            setHasStableIds(true)
        }
    }

    if (items != null) {
        (adapter as PloggingHistoryAdapter).submitList(items)
    }
}

@SuppressLint("NotifyDataSetChanged")
@BindingAdapter("challenges")
fun RecyclerView.setChallenge(items: List<Challenge>) {
    if (adapter == null) {
        adapter = ChallengeAdapter().apply {
            setHasStableIds(true)
        }
    }

    (adapter as ChallengeAdapter).submitList(items)

}

@BindingAdapter("challengeStatus")
fun ViewGroup.setChallengeBackground(status: ChallengeStatus) {
    when (status) {
        ChallengeStatus.BEFORE -> setBackgroundResource(R.drawable.background_item_challenge_before)
        ChallengeStatus.NOW -> setBackgroundResource(R.drawable.background_item_challenge_now)
        ChallengeStatus.DONE -> setBackgroundResource(R.drawable.background_item_challenge_done)
    }
}

@BindingAdapter(value = ["challengeStatus", "challengeAchieved"], requireAll = false)
fun TextView.setTextStyle(status: ChallengeStatus, challengeAchieved: Boolean?) {
    Log.d(TAG, "text: $text, status: $status, achieved: $challengeAchieved")

    var bulletColor: Int = ContextCompat.getColor(context, R.color.black)
    if (status == ChallengeStatus.DONE) {
        paintFlags = 0
        bulletColor = ContextCompat.getColor(context, R.color.white)
        setTextColor(bulletColor)
    } else if (status == ChallengeStatus.NOW && challengeAchieved == true) {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    if (challengeAchieved != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        text = SpannableStringBuilder(text.toString()).apply {
            setSpan(
                BulletSpan(24, bulletColor, 6),
                0, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }
}

@BindingAdapter("ranking")
fun RecyclerView.setRanking(items: List<RankingUser>?) {
    if (items == null) return

    if (adapter == null) {
        adapter = RankingAdapter()
    }

    (adapter as RankingAdapter).replaceAll(items)
    adapter!!.notifyDataSetChanged()

}


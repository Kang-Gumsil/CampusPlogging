package dku.gyeongsotone.gulging.campusplogging.data.local.model

import android.graphics.Bitmap

/**
 * indicates the status of enrollment certification
 *
 */
enum class UnivCertStatus {
    DONE, DOING, TO_DO
}

/**
 * user class
 *
 */
data class User(
    val userId: String,
    val nickname: String? = null,
    val studentId: String? = null,
    val univCertStatus: UnivCertStatus,
    val profileImage: Bitmap
)

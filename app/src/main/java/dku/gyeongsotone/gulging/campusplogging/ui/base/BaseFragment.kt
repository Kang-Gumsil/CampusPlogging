package dku.gyeongsotone.gulging.campusplogging.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    abstract fun observeViewModel()
    protected abstract fun initViewBinding()


}
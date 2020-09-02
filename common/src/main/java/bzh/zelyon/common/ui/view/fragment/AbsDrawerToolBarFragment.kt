package bzh.zelyon.common.ui.view.fragment

import android.os.Bundle
import android.view.View
import bzh.zelyon.common.R
import bzh.zelyon.common.ui.view.activity.AbsDrawerActivity

abstract class AbsDrawerToolBarFragment: AbsToolBarFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (absActivity is AbsDrawerActivity) {
            toolbar?.apply {
                setNavigationIcon(R.drawable.ic_menu)
                setNavigationOnClickListener {
                    (absActivity as AbsDrawerActivity).openDrawer()
                }
            }
        }
    }
}
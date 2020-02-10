package bzh.zelyon.music.ui.view.abs.fragment

import android.os.Bundle
import android.view.View
import bzh.zelyon.music.R
import bzh.zelyon.music.ui.view.abs.activity.AbsDrawerActivity

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
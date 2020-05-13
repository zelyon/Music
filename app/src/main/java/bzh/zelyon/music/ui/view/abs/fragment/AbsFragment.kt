package bzh.zelyon.music.ui.view.abs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import bzh.zelyon.music.R
import bzh.zelyon.music.ui.view.abs.activity.AbsActivity

abstract class AbsFragment: Fragment() {

    lateinit var absActivity: AbsActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        absActivity = activity as AbsActivity

        sharedElementEnterTransition = android.transition.TransitionInflater.from(absActivity).inflateTransition(R.transition.enter_transition)
        exitTransition = android.transition.TransitionInflater.from(absActivity).inflateTransition(R.transition.exit_transition)
        postponeEnterTransition()
        startPostponedEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(getIdLayout(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyOnClickListener(view)
    }

    private fun applyOnClickListener(view: View) {
        if (!view.hasOnClickListeners()) {
            view.setOnClickListener { onIdClick(it.id) }
        }
        if (view is ViewGroup) {
            view.children.forEach { applyOnClickListener(it) }
        }
    }

    abstract fun getIdLayout(): Int

    open fun onIdClick(id: Int) {}

    open fun onBackPressed() = true

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = true, transitionView: View? = null) {
        absActivity.showFragment(fragment, addToBackStack, transitionView)
    }

    fun fullBack() {
        absActivity.fullBack()
    }

    fun back(nb: Int = 1) {
        absActivity.back(nb)
    }

    fun safeRun(action: () -> Unit) {
        if (isVisible) {
            absActivity.runOnUiThread(action)
        }
    }
}
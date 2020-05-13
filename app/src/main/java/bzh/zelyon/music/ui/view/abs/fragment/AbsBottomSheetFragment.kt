package bzh.zelyon.music.ui.view.abs.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import bzh.zelyon.music.ui.view.abs.activity.AbsActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class AbsBottomSheetFragment: BottomSheetDialogFragment() {

    lateinit var absActivity: AbsActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        absActivity = activity as AbsActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(getLayoutId(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyOnClickListener(view)
    }

    private fun applyOnClickListener(view: View) {
        if (!view.hasOnClickListeners()) {
            view.setOnClickListener { onIdClick(it.id) }
        }
        if (view is ViewGroup) {
            view.children.forEach {
                applyOnClickListener(it)
            }
        }
    }

    abstract fun getLayoutId(): Int

    open fun onIdClick(id: Int) {}

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = true, transitionView: View? = null) {
        absActivity.showFragment(fragment, addToBackStack, transitionView)
    }

    fun safeRun(action: () -> Unit) {
        if (isVisible) {
            absActivity.runOnUiThread(action)
        }
    }

    fun back() {
        dismiss()
    }
}
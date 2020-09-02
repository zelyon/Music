package bzh.zelyon.common.ui.view.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import bzh.zelyon.common.R

abstract class AbsToolBarBottomSheetFragment: AbsBottomSheetFragment() {

    var menu: Menu? = null
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(getIdToolbar())
        toolbar?.apply {
            title = getTitleToolBar()
            subtitle = getSubTitleToolBar()
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener {
                dismiss()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (getIdMenu() != -1) {
            inflater.inflate(getIdMenu(), menu)
            this.menu = menu
            onUpdateMenu()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onIdClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    abstract fun getIdToolbar(): Int

    open fun getTitleToolBar() = ""

    open fun getSubTitleToolBar() = ""

    open fun getIdMenu() = R.menu.empty

    open fun onUpdateMenu() {}
}
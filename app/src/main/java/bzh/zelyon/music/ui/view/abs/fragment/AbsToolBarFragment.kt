package bzh.zelyon.music.ui.view.abs.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import bzh.zelyon.music.R

abstract class AbsToolBarFragment: AbsFragment() {

    var menu: Menu? = null
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(getIdToolbar())
        toolbar?.let {
            absActivity.setSupportActionBar(it)
            updateToolBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(getIdMenu(), menu)
        this.menu = menu
        onUpdateMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onIdClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        if (id == android.R.id.home) {
            absActivity.onBackPressed()
        }
    }

    abstract fun getIdToolbar(): Int

    fun updateToolBar() {
        absActivity.supportActionBar?.title = getTitleToolBar()
        absActivity.supportActionBar?.subtitle = getSubTitleToolBar()
        absActivity.supportActionBar?.setDisplayHomeAsUpEnabled(showBack())
    }

    open fun getTitleToolBar() = ""

    open fun getSubTitleToolBar() = ""

    open fun showBack() = absActivity.supportFragmentManager.backStackEntryCount > 0

    open fun getIdMenu() = R.menu.empty

    open fun onUpdateMenu() {}
}
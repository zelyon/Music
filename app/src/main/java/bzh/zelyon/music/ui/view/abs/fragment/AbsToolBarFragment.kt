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
            absActivity.supportActionBar?.title = getToolBarTitle()
            absActivity.supportActionBar?.subtitle = getToolBarSubTitle()
            absActivity.supportActionBar?.setDisplayHomeAsUpEnabled(showBack())
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
            back()
        }
    }

    abstract fun getIdToolbar(): Int

    open fun getToolBarTitle() = ""

    open fun getToolBarSubTitle() = ""

    open fun showBack() = absActivity.supportFragmentManager.backStackEntryCount > 0

    open fun getIdMenu() = R.menu.empty

    open fun onUpdateMenu() {}
}
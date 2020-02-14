package bzh.zelyon.music.ui.view.abs.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import bzh.zelyon.music.ui.view.abs.fragment.AbsBottomSheetFragment
import bzh.zelyon.music.ui.view.abs.fragment.AbsFragment
import com.google.android.material.snackbar.Snackbar

abstract class AbsActivity: AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 4
    }

    private var intentResult:(Int, Intent) -> Unit = { _, _ -> }
    private var permissionsResult:(Boolean) -> Unit = { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        intent?.let {
            handleIntent(it)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIntent(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            data?.let {
                intentResult.invoke(resultCode, data)
                intentResult = { _, _ -> }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionsResult.invoke(true)
                permissionsResult = {}
            } else {
                permissionsResult.invoke(false)
            }
        }
    }

    override fun onBackPressed() {
        if (getCurrentFragment()?.onBackPressed() == true) {
            super.onBackPressed()
        }
    }

    abstract fun getLayoutId(): Int

    abstract fun getFragmentContainerId(): Int

    open fun handleIntent(intent: Intent) {}

    fun getCurrentFragment() = supportFragmentManager.findFragmentById(getFragmentContainerId()) as? AbsFragment

    fun startIntentWithResult(intent: Intent, intentResult:(Int, Intent) -> Unit) {
        this.intentResult = intentResult
        startActivityForResult(intent, REQUEST_CODE)
    }

    fun ifPermissions(vararg permissions: String, permissionsResult:(Boolean) -> Unit) {
        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            permissionsResult.invoke(true)
        } else {
            this.permissionsResult = permissionsResult
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
    }

    fun snackBar(message: String, duration: Int = Snackbar.LENGTH_LONG, actionMessage: String? = null, actionResult:() -> Unit = {}) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).apply {
            setAction(actionMessage) {
                actionResult.invoke()
            }
        }.show()
    }

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = true, transitionView: View? = null) {
        if (fragment is AbsFragment) {
            supportFragmentManager.beginTransaction().replace(getFragmentContainerId(), fragment).apply {
                if (addToBackStack && getCurrentFragment() != null) {
                    addToBackStack(this::class.java.name)
                }
                transitionView?.let {
                    setReorderingAllowed(true).addSharedElement(transitionView, transitionView.transitionName)
                }
            }.commit()
        }
        else if (fragment is AbsBottomSheetFragment) {
            fragment.show(supportFragmentManager, fragment::class.java.name)
        }
    }

    fun fullBack() {
        back(supportFragmentManager.backStackEntryCount)
    }

    fun back(nb: Int = 1) {
        for (i in 0..nb) {
            supportFragmentManager.popBackStack()
        }
    }
}
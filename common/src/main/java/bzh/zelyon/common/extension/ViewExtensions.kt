package bzh.zelyon.common.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.openKeyboard() {
    requestFocus()
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, 0)
}

fun View.closeKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}

fun View.vibrate(step: Int = 0) {
    val translations = arrayOf(0F, 25F, -25F, 25F, -25F, 15F, -15F, 5F, -5F, 0F)
    animate().apply {
        duration = 100L
        translationX(translations[step])
        withEndAction { if (step < translations.size-1) vibrate(step + 1) }
    }.start()
}
package bzh.zelyon.common.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import bzh.zelyon.common.R

class SquareView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {

    private var horizontal = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SquareView, defStyleAttr, 0).apply {
            horizontal = getBoolean(R.styleable.SquareView_horizontal, false)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureSpecOrientation = if (horizontal) heightMeasureSpec else widthMeasureSpec
        val measureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(measureSpecOrientation), MeasureSpec.EXACTLY)
        super.onMeasure(measureSpec, measureSpec)
    }
}
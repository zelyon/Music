package bzh.zelyon.music.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bzh.zelyon.music.R
import bzh.zelyon.music.utils.dpToPx
import bzh.zelyon.music.utils.getResIdFromAndroidAttr
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

class ItemsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RecyclerView(context, attrs, defStyleAttr) {

    var items: MutableList<*> = mutableListOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var nbColumns = 1
        set(value) {
            field = value
            layoutManager = when (value) {
                1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, value).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int) = if (position == 0 || position == items.size + 1) value else 1
                    }
                }
            }
            notifyDataSetChanged()
        }

    var idLayoutItem = R.layout.item_empty
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var idLayoutHeader = R.layout.item_empty
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var idLayoutFooter = R.layout.item_empty
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var idLayoutEmpty = R.layout.item_empty
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var helper: Helper? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dragNDropEnable = false
        set(value) {
            field = value
            itemTouchHelper.attachToRecyclerView(if (value || swipeEnable) this else null)
        }

    var swipeEnable = false
        set(value) {
            field = value
            itemTouchHelper.attachToRecyclerView(if (value || dragNDropEnable) this else null)
        }

    var spaceDivider: Int = 0
        set(value) {
            field = value
            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
                    if (spaceDivider > 0) {
                        var position = parent.getChildAdapterPosition(view)
                        if (position != 0 && position != items.size + 1 && !items.isNullOrEmpty()) {
                            position--
                            outRect.top = if (position < nbColumns) value else value / 2
                            outRect.left = if (position % nbColumns == 0) value else value / 2
                            outRect.right = if ((position + 1) % nbColumns == 0) value else value / 2
                            outRect.bottom = if (position > items.size - nbColumns) value else value / 2
                        }
                    }
                }
            })
            notifyDataSetChanged()
        }

    var isFastScroll: Boolean = false
        set(value) {
            field = value
            if (value) {
                addOnItemTouchListener(scrollItemDecorator)
                addItemDecoration(scrollItemDecorator)
            } else {
                removeOnItemTouchListener(scrollItemDecorator)
                removeItemDecoration(scrollItemDecorator)
            }
            notifyDataSetChanged()
        }

    @ColorInt
    var thumbColor = context.getColor(context.getResIdFromAndroidAttr(android.R.attr.colorAccent))
    @ColorInt
    var thumbColorDisable =  context.getColor(android.R.color.darker_gray)
    @ColorInt
    var thumbTextColor =  context.getColor(android.R.color.white)
    var thumbMinHeight = context.dpToPx(36)
    var thumbWidth = context.dpToPx(4)
    var thumbCorner = context.dpToPx(8)
    var thumbMarginLeft = context.dpToPx(8)
    var thumbMarginRight = context.dpToPx(0)
    var thumbMarginTop = context.dpToPx(0)
    var thumbMarginBottom = context.dpToPx(0)
    var thumbTextSize = context.dpToPx(32)

    private var thumbHeight = 0F
    private var thumbTop = 0F
    private val thumbBottom get() = thumbTop + thumbHeight
    private val thumbLeft get() = right - thumbMarginRight - thumbWidth
    private val thumbRight get() = thumbLeft + thumbWidth
    private val thumbCenterY get() = thumbTop + thumbHeight/2
    private val thumbMinY get() = top + thumbMarginTop
    private val thumbMaxY get() = bottom - thumbMarginBottom
    private val thumbScrollingHeight get() = height - thumbMarginTop - thumbMarginBottom
    private val thumbMoveHeight get() = computeVerticalScrollRange() - thumbMarginTop - thumbMarginBottom
    private var thumbNeedShow = false
    private var thumbDragging = false

    private val scrollItemDecorator = object : ItemDecoration(), OnItemTouchListener {

        private var motionEventY = 0F

        override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: State) {
            if (thumbNeedShow) {
                val thumbPaint = Paint().apply {
                    color = if (thumbDragging) thumbColor else thumbColorDisable
                    isAntiAlias = true
                }
                canvas.drawRoundRect(thumbLeft, thumbTop, thumbRight, thumbBottom, thumbCorner, thumbCorner, thumbPaint)
                if (thumbDragging) {
                    val position = round(((items.size-1) * (thumbTop - thumbMarginTop) / thumbScrollingHeight)).toInt()
                    helper?.getIndexScroll(items, position)?.let { index ->
                        val centerX = thumbLeft - thumbMarginLeft - thumbTextSize/4 - thumbTextSize
                        val centerY = when {
                            thumbCenterY - thumbTextSize < thumbMinY -> thumbMinY + thumbTextSize
                            thumbCenterY + thumbTextSize > thumbMaxY -> thumbMaxY - thumbTextSize
                            else -> thumbCenterY
                        }
                        canvas.apply {
                            save()
                            rotate(-45F, centerX, centerY)
                            drawCircle(centerX, centerY, thumbTextSize, thumbPaint)
                            drawRoundRect(centerX, centerY, centerX + thumbTextSize, centerY + thumbTextSize, thumbCorner, thumbCorner, thumbPaint)
                            drawRect(centerX, centerY, centerX + thumbCorner, centerY + thumbTextSize, thumbPaint)
                            drawRect(centerX, centerY, centerX + thumbTextSize, centerY + thumbCorner, thumbPaint)
                            restore()
                            drawText(index, centerX, centerY + thumbTextSize/3, Paint().apply {
                                textSize = thumbTextSize
                                textAlign = Paint.Align.CENTER
                                color = thumbTextColor
                            })
                        }
                    }
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        override fun onTouchEvent(recyclerView: RecyclerView, ev: MotionEvent) {
            onInterceptTouchEvent(ev)
        }

        override fun onInterceptTouchEvent(recyclerView: RecyclerView, ev: MotionEvent) = onMotionEventIsDragging(ev)

        private fun onMotionEventIsDragging(motionEvent: MotionEvent) =
            if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_DOWN &&
                motionEvent.x > thumbLeft - thumbMarginLeft &&
                motionEvent.y in thumbTop..thumbBottom) {
                motionEventY = motionEvent.y
                thumbDragging = true
                true
            } else if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_UP &&
                thumbDragging) {
                motionEventY = 0f
                thumbDragging = false
                false
            } else if (thumbNeedShow &&
                motionEvent.action == MotionEvent.ACTION_MOVE &&
                thumbDragging && abs(thumbTop - motionEvent.y) >= 2) {
                val totalPossibleOffset = (thumbMoveHeight - thumbScrollingHeight).toInt()
                val scrollingBy = ((motionEvent.y - motionEventY) / thumbScrollingHeight * totalPossibleOffset).toInt()
                if (computeVerticalScrollOffset() + scrollingBy in 0 until totalPossibleOffset) {
                    scrollBy(0, scrollingBy)
                }
                motionEventY = motionEvent.y
                true
            } else false

        fun onScroll() {
            if (thumbMoveHeight > thumbScrollingHeight) {
                thumbNeedShow = true
                thumbHeight = max(thumbScrollingHeight.pow(2) / thumbMoveHeight, thumbMinHeight)
                thumbTop = computeVerticalScrollOffset() * thumbScrollingHeight / thumbMoveHeight + thumbMarginTop
            } else if (thumbNeedShow) {
                thumbNeedShow = false
            }
        }
    }

    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            helper?.onScroll(dy <= 0)
            if (isFastScroll) {
                scrollItemDecorator.onScroll()
            }
        }
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder) = makeMovementFlags(when {
            dragNDropEnable && nbColumns == 1 -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
            dragNDropEnable -> ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            else -> 0
        }, if (swipeEnable && nbColumns == 1) ItemTouchHelper.START or ItemTouchHelper.END else 0)

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            var sourcePosition = viewHolder.adapterPosition
            var targetPosition = target.adapterPosition
            return if (sourcePosition in 1..items.size && targetPosition in 1..items.size) {
                adapter?.notifyItemMoved(sourcePosition, targetPosition)
                sourcePosition--
                targetPosition--
                if (sourcePosition in items.indices && targetPosition in items.indices) {
                    Collections.swap(items, sourcePosition, targetPosition)
                    helper?.onItemsMove(items)
                }
                true
            } else false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter?.notifyItemRemoved(position)
            items.removeAt(position - 1)
            helper?.onItemSwipe(viewHolder.itemView, items, position - 1)
            adapter?.notifyDataSetChanged()
        }

        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.let {
                    val position = viewHolder.adapterPosition
                    helper?.onItemStartDrag(viewHolder.itemView, items, position - 1)
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            val position = viewHolder.adapterPosition
            if (position != -1) {
                helper?.onItemEndDrag(viewHolder.itemView, items, position - 1)
            }
        }

        override fun isItemViewSwipeEnabled() = true

        override fun isLongPressDragEnabled() = false
    })

    private val itemsAdapter = object : Adapter<ViewHolder>() {

        private val DATA_TYPE = 0
        private val HEADER_TYPE = 1
        private val EMPTY_TYPE = 2
        private val FOOTER_TYPE = 3

        override fun getItemCount() = items.size + if (items.isEmpty()) 3 else 2

        override fun getItemViewType(position: Int) = when {
            position == 0 -> HEADER_TYPE
            position == itemCount - 1 -> FOOTER_TYPE
            items.isEmpty() -> EMPTY_TYPE
            else -> DATA_TYPE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = object : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(when (viewType) {
            HEADER_TYPE -> idLayoutHeader
            FOOTER_TYPE -> idLayoutFooter
            EMPTY_TYPE -> idLayoutEmpty
            else -> idLayoutItem
        }, parent, false)) {}

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                HEADER_TYPE -> helper?.onBindHeader(viewHolder.itemView)
                FOOTER_TYPE -> helper?.onBindFooter(viewHolder.itemView)
                EMPTY_TYPE -> helper?.onBindEmpty(viewHolder.itemView)
                DATA_TYPE ->  {
                    helper?.onBindItem(viewHolder.itemView, items, position - 1)
                    viewHolder.itemView.setOnClickListener {
                        helper?.onItemClick(viewHolder.itemView, items, position - 1)
                    }
                    viewHolder.itemView.setOnLongClickListener {
                        helper?.onItemLongClick(viewHolder.itemView, items, position - 1)
                        true
                    }

                    helper?.getDragView(viewHolder.itemView, items, position - 1)?.let { dragView ->
                        dragView.isVisible = items.size > 1
                        dragView.setOnTouchListener { _, event ->
                            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                                itemTouchHelper.startDrag(viewHolder)
                            }
                            return@setOnTouchListener true
                        }
                    }
                }
            }
        }
    }

    init {
        nbColumns = 1
        adapter = itemsAdapter
        setHasFixedSize(false)
        addOnScrollListener(scrollListener)
    }

    fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    open class Helper {
        open fun onScroll(goUp: Boolean) {}

        open fun onBindHeader(headerView: View) {}
        open fun onBindFooter(footerView: View) {}
        open fun onBindEmpty(emptyView: View) {}

        open fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemClick(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemLongClick(itemView: View, items: MutableList<*>, position: Int) {}

        open fun getIndexScroll(items: MutableList<*>, position: Int): String? = null

        open fun getDragView(itemView: View, items: MutableList<*>, position: Int): View? = null
        open fun onItemsMove(items: MutableList<*>) {}
        open fun onItemStartDrag(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemEndDrag(itemView: View, items: MutableList<*>, position: Int) {}
        open fun onItemSwipe(itemView: View, items: MutableList<*>, position: Int) {}
    }
}
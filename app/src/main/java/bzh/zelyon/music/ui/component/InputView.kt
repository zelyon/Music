package bzh.zelyon.music.ui.component

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.CalendarContract
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import bzh.zelyon.music.R
import bzh.zelyon.music.ui.view.abs.activity.AbsActivity
import bzh.zelyon.music.utils.closeKeyboard
import bzh.zelyon.music.utils.vibrate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.item_input_list.view.*
import kotlinx.android.synthetic.main.view_input.view.*
import java.text.SimpleDateFormat
import java.util.*

class InputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): FrameLayout(context, attrs, defStyleAttr) {

    enum class Type {TEXT, MULTI_LINE, NUMBER, DECIMAL, EMAIL, PHONE, PASSWORD, PIN, DATE, DATE_TIME, TIME, LIST, LIST_CUSTOM, LIST_MULTI, LIST_CUSTOM_MULTI}

    var label = ""
        set(value) {
            field = value
            view_input_layout.hint = value + if (mandatory) " *" else ""
        }

    var mandatory = false
        set(value) {
            field = value
            view_input_layout.hint = label + if (value) " *" else ""
        }

    var type = Type.TEXT
        set(value) {
            field = value
            when (value) {
                Type.TEXT -> view_input_edittext.inputType = EditorInfo.TYPE_CLASS_TEXT
                Type.MULTI_LINE -> view_input_edittext.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                Type.NUMBER -> view_input_edittext.inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_NORMAL
                Type.DECIMAL -> view_input_edittext.inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
                Type.EMAIL -> {
                    setEndIcon(EndIcon(context.getDrawable(R.drawable.ic_email)) {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$text")))
                    })
                    view_input_edittext.inputType = EditorInfo.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }
                Type.PHONE -> {
                    setEndIcon(EndIcon(context.getDrawable(R.drawable.ic_call)) {
                        (context as? AbsActivity)?.ifPermissions(Manifest.permission.CALL_PHONE) {
                            if (it) {
                                context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$text")))
                            }
                        }
                    })
                    view_input_edittext.inputType = EditorInfo.TYPE_CLASS_PHONE
                    view_input_edittext.addTextChangedListener(PhoneNumberFormattingTextWatcher())
                }
                Type.PASSWORD -> {
                    view_input_layout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    view_input_edittext.inputType = EditorInfo.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    view_input_edittext.typeface = Typeface.DEFAULT
                    isMenuContextEnabled = false
                }
                Type.PIN -> {
                    view_input_layout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    view_input_edittext.inputType = EditorInfo.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    view_input_edittext.typeface = Typeface.DEFAULT
                    isMenuContextEnabled = false
                }
                Type.DATE -> {
                    setEndIcon(EndIcon(context.getDrawable(R.drawable.ic_event)) {
                        context.startActivity(Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date?.time ?: 0)
                        })
                    })
                    dateFormat = "dd/MM/YYYY"
                    onAction { showDate(Calendar.getInstance().apply { time = date ?: Date() }, false) }
                }
                Type.DATE_TIME -> {
                    setEndIcon(EndIcon(context.getDrawable(R.drawable.ic_event)) {
                        context.startActivity(Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date?.time ?: 0)
                        })
                    })
                    dateFormat = "dd/MM/YYYY hh:mm"
                    onAction { showDate(Calendar.getInstance().apply { time = date ?: Date() }, true) }
                }
                Type.TIME -> {
                    setEndIcon(EndIcon(context.getDrawable(R.drawable.ic_event)) {
                        context.startActivity(Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date?.time ?: 0)
                        })
                    })
                    dateFormat = "hh:mm"
                    onAction { showTime(Calendar.getInstance().apply { time = date ?: Date() }) }
                }
                Type.LIST -> onAction { showList(false, false) }
                Type.LIST_CUSTOM -> onAction { showList(true, false) }
                Type.LIST_MULTI ->  onAction { showList(false, true) }
                Type.LIST_CUSTOM_MULTI -> onAction { showList(true, true) }
            }
        }

    var minLength: Int? = null
        set(value) {
            field = if (value != -1) value else null
        }
    var maxLength: Int? = null
        set(value) {
            field = if (value != -1) value else null
            view_input_layout.isCounterEnabled = field != null
            view_input_layout.counterMaxLength = field ?: Int.MAX_VALUE
        }

    var isMenuContextEnabled: Boolean = true
        set(value) {
            field = value
            view_input_edittext.customSelectionActionModeCallback = if (value) null else object : MultiChoiceModeListener {
                override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {}
                override fun onDestroyActionMode(mode: ActionMode?) {}
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            }
        }

    var text: String? = null
        get() = view_input_edittext.text.toString()
        set(value) {
            field = value
            view_input_edittext.setText(value.orEmpty())
        }

    var number: Int? = null
        get() = text?.toInt()
        set(value) {
            field = value
            text = value.toString()
        }
    var minNumber: Int? = null
        set(value) {
            field = if (value != -1) value else null
        }
    var maxNumber: Int? = null
        set(value) {
            field = if (value != -1) value else null
        }

    var decimal: Float? = null
        get() = text?.toFloat()
        set(value) {
            field = value
            text = value.toString()
        }
    var minDecimal: Int? = null
        set(value) {
            field = if (value != -1) value else null
        }
    var maxDecimal: Int? = null
        set(value) {
            field = if (value != -1) value else null
        }

    var date: Date? = null
        set(value) {
            field = value
            text = if (value == null) "" else SimpleDateFormat(dateFormat).format(value.time)
        }
    var dateMin: Date? = null
    var dateMax: Date? = null
    var dateFormat = ""

    var choices = mutableListOf<Choice>()
        set(value) {
            field = value
            selectedChoices = value.filter { it.selected }.toMutableList()
        }
    var selectedChoices = mutableListOf<Choice>()
        set(value) {
            field = value
            text = value.joinToString(separator = ", ") { it.label }
        }
    val selectedChoice get() = selectedChoices.firstOrNull()
    var choicesPopup: Dialog? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.InputView, defStyleAttr, 0).apply {
            label = getString(R.styleable.InputView_label).orEmpty()
            mandatory = getBoolean(R.styleable.InputView_mandatory, false)
            type = when (getInt(R.styleable.InputView_type, 0)) {
                0 -> Type.TEXT
                1 -> Type.MULTI_LINE
                2 -> Type.NUMBER
                3 -> Type.DECIMAL
                4 -> Type.EMAIL
                5 -> Type.PHONE
                6 -> Type.PASSWORD
                7 -> Type.PIN
                8 -> Type.DATE
                9 -> Type.DATE_TIME
                10 -> Type.TIME
                11 -> Type.LIST
                12 -> Type.LIST_CUSTOM
                13 -> Type.LIST_MULTI
                14 -> Type.LIST_CUSTOM_MULTI
                else -> Type.TEXT
            }
            minLength = getInt(R.styleable.InputView_min_length, -1)
            maxLength = getInt(R.styleable.InputView_max_length, -1)
            minNumber = getInt(R.styleable.InputView_min_number, -1)
            maxNumber = getInt(R.styleable.InputView_max_number, -1)
            minDecimal = getInt(R.styleable.InputView_min_decimal, -1)
            maxDecimal = getInt(R.styleable.InputView_max_decimal, -1)
            recycle()
        }

        onTextChange {
            if (view_input_layout.isErrorEnabled) {
                checkValidity(false)
            }
        }

        view_input_layout.errorIconDrawable = null
    }

    private fun showDate(calendar: Calendar, showTime: Boolean) {
        DatePickerDialog(context,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                if (showTime) {
                    showTime(calendar)
                } else {
                    date = calendar.time
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).apply {
            dateMin?.let { datePicker.minDate = it.time }
            dateMax?.let { datePicker.maxDate = it.time }
        }.show()
    }

    private fun showTime(calendar: Calendar) {
        TimePickerDialog(context,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                date = calendar.time
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true).show()
    }

    private fun showList(custom: Boolean, multi: Boolean) {
        choicesPopup?.dismiss()
        choicesPopup = BottomSheetDialog(context).apply {
            val finalChoices = choices
            val itemsView = ItemsView(context).apply {
                idLayoutItem = R.layout.item_input_list
                helper = object : ItemsView.Helper() {
                    override fun onBindItem(itemView: View, items: MutableList<*>, position: Int) {
                        val choice = items[position]
                        if (choice is Choice) {
                            itemView.item_input_list_textview.text = choice.label
                            choice.icon?.let { icon ->
                                itemView.item_input_list_imageview.setImageDrawable(icon)
                            }
                            itemView.item_input_list_checkbox.isVisible = multi
                        }
                    }
                    override fun onItemClick(itemView: View, itemList: MutableList<*>, position: Int) {
                        val choice = items[position]
                        if (choice is Choice) {
                            if (choice.children.isNullOrEmpty()) {
                                if (multi) {
                                    if (selectedChoices.contains(choice)) {
                                        selectedChoices.remove(choice)
                                    } else {
                                        selectedChoices.add(choice)
                                    }
                                } else {
                                    selectedChoices = mutableListOf(choice)
                                    dismiss()
                                }
                                text = selectedChoices.joinToString(separator = ", ") { it.label }
                            } else {
                                items = choice.children
                            }
                        }
                    }
                }
                items = finalChoices
            }
            val toolbar = Toolbar(context).apply {
                title = label
                setNavigationIcon(R.drawable.ic_close)
                setNavigationOnClickListener {
                    dismiss()
                }
                if (multi) {
                    inflateMenu(R.menu.inputview)
                    setOnMenuItemClickListener {
                        dismiss()
                        true
                    }
                }
            }
            val inputView = InputView(context).apply {
                label = context.getString(if (custom) R.string.inputview_search_or_add else R.string.inputview_search)
                onTextChange { search ->
                    itemsView.items = finalChoices.filter {
                        it.label.toLowerCase(Locale.getDefault()).contains(search.toLowerCase(Locale.getDefault()))
                    }.toMutableList().apply {
                        if (custom && search.isNotBlank()) {
                            add(0, Choice(search, search))
                        }
                    }
                }
            }
            setContentView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(toolbar)
                addView(inputView)
                addView(itemsView)
            })
        }
        choicesPopup?.show()
    }

    private fun onAction(action:() -> Unit) {
        view_input_edittext.inputType = InputType.TYPE_NULL
        listOf(this, view_input_edittext).forEach {
            it.setOnFocusChangeListener { _, hasFocus ->
                closeKeyboard()
                if (hasFocus) {
                    action.invoke()
                }
            }
            it.setOnClickListener {
                closeKeyboard()
                action.invoke()
            }
        }
    }

    fun checkValidity(vibrate: Boolean = true): Boolean {
        val errorsMessages = mutableListOf<String>()
        if (mandatory && text.isNullOrBlank()) {
            errorsMessages.add(context.getString(R.string.inputview_field_mandatory))
        }
        text?.trim()?.length?.let { length ->
            minLength?.let { minLength ->
                if (length < minLength) {
                    errorsMessages.add(context.getString(R.string.inputview_field_min_lenght, minLength))
                }
            }
            maxLength?.let { maxLength ->
                if (length > maxLength) {
                    errorsMessages.add(context.getString(R.string.inputview_field_max_lenght, maxLength))
                }
            }
        }
        when (type) {
            Type.NUMBER -> {
                number?.let { number ->
                    minNumber?.let { minNumber ->
                        if (number < minNumber) {
                            errorsMessages.add(context.getString(R.string.inputview_field_min_number, minNumber))
                        }
                    }
                    maxNumber?.let { maxNumber ->
                        if (number > maxNumber) {
                            errorsMessages.add(context.getString(R.string.inputview_field_max_number, maxNumber))
                        }
                    }
                }
            }
            Type.DECIMAL -> {
                decimal?.let { decimal ->
                    minDecimal?.let { minDecimal ->
                        if (decimal < minDecimal) {
                            errorsMessages.add(context.getString(R.string.inputview_field_min_number, minDecimal))
                        }
                    }
                    maxDecimal?.let { maxDecimal ->
                        if (decimal > maxDecimal) {
                            errorsMessages.add(context.getString(R.string.inputview_field_max_number, maxDecimal))
                        }
                    }
                }}
            Type.PHONE -> {
                if (!Patterns.PHONE.matcher(text.orEmpty()).matches()) {
                    errorsMessages.add(context.getString(R.string.inputview_field_phone))
                }
            }
            Type.EMAIL -> {
                if (!Patterns.EMAIL_ADDRESS.matcher(text.orEmpty()).matches()) {
                    errorsMessages.add(context.getString(R.string.inputview_field_email))
                }
            }
        }

        val noErrors = errorsMessages.isEmpty()
        view_input_layout.isErrorEnabled = !noErrors
        view_input_layout.error = errorsMessages.joinToString(separator = "\n")
        if (!noErrors && vibrate) {
            vibrate()
        }
        return noErrors
    }

    fun selectAll() = view_input_edittext.selectAll()

    fun onTextChange(action: (String) -> Unit) {
        view_input_edittext.addTextChangedListener {
            action.invoke(it.toString())
        }
    }

    fun setEndIcon(endIcon: EndIcon?) {
        endIcon?.let {
            when (endIcon) {
                is TogglePasswordEndIcon -> {
                    view_input_layout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
                is ClearTextEndIcon -> {
                    view_input_layout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
                else -> {
                    view_input_layout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    view_input_layout.endIconDrawable = endIcon.icon
                    view_input_layout.setEndIconOnClickListener {
                        endIcon.action?.invoke()
                    }
                }
            }
        } ?: run {
            view_input_layout.endIconMode = TextInputLayout.END_ICON_NONE
        }
    }

    open class EndIcon(val icon: Drawable?, val action: (() -> Unit)?)

    class TogglePasswordEndIcon: EndIcon(null, null)

    class ClearTextEndIcon: EndIcon(null, null)

    class InfoEndIcon(val context: Context, infos: String): EndIcon(context.getDrawable(R.drawable.ic_info), { AlertDialog.Builder(context).setMessage(infos).show() })

    class Choice(val label: String, val value: Any, val selected: Boolean = false, val icon: Drawable? = null, val children: MutableList<Choice>? = null)
}
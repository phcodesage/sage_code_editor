package com.example.sagecodeeditor.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sagecodeeditor.R

/**
 * Custom EditText with line numbers
 */
class LineNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : LinearLayout(context, attrs, defStyleAttr) {

    private val lineNumberTextView: TextView
    private val codeEditText: EditText
    private val scrollView: ScrollView
    private val horizontalScrollView: HorizontalScrollView

    private val textPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = 40f
        typeface = Typeface.MONOSPACE
    }

    init {
        orientation = HORIZONTAL

        // Line numbers view
        lineNumberTextView = TextView(context).apply {
            setTextColor(Color.GRAY)
            gravity = Gravity.END
            typeface = Typeface.MONOSPACE
            setPadding(8, 0, 8, 0)
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Dark background
        }

        // Code edit text
        codeEditText = EditText(context).apply {
            background = null
            gravity = Gravity.TOP
            typeface = Typeface.MONOSPACE
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1E1E1E")) // Dark background
            setHorizontallyScrolling(true)
        }

        // Horizontal scroll view for code
        horizontalScrollView = HorizontalScrollView(context).apply {
            addView(codeEditText)
        }

        // Scroll view for both
        scrollView = ScrollView(context).apply {
            addView(horizontalScrollView)
        }

        // Add views to layout
        addView(lineNumberTextView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
        addView(scrollView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Update line numbers when text changes
        codeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateLineNumbers()
            }
        })

        // Initial line numbers
        updateLineNumbers()
    }

    private fun updateLineNumbers() {
        val text = codeEditText.text.toString()
        val lineCount = text.split("\n").size
        val lineNumbers = StringBuilder()
        
        for (i in 1..lineCount) {
            lineNumbers.append(i).append("\n")
        }
        
        lineNumberTextView.text = lineNumbers.toString()
    }

    // Getter and setter for text
    fun setText(text: CharSequence) {
        codeEditText.setText(text)
        updateLineNumbers()
    }

    fun getText(): Editable {
        return codeEditText.text
    }

    // Expose the EditText for syntax highlighting
    fun getEditText(): EditText {
        return codeEditText
    }

    // Set selection
    fun setSelection(index: Int) {
        codeEditText.setSelection(index)
    }

    // Get selection
    fun getSelectionStart(): Int {
        return codeEditText.selectionStart
    }
}

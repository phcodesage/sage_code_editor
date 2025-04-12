package com.example.sagecodeeditor.util

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import java.util.regex.Pattern

class SyntaxHighlighter {
    // Flag to prevent recursive calls during text change
    private var isHighlighting = false
    
    // Colors for syntax highlighting
    private val keywordColor = Color.rgb(86, 156, 214)    // Blue
    private val stringColor = Color.rgb(206, 145, 120)    // Orange-brown
    private val commentColor = Color.rgb(87, 166, 74)     // Green
    private val numberColor = Color.rgb(181, 206, 168)    // Light green
    private val tagColor = Color.rgb(197, 134, 192)       // Purple
    private val attributeColor = Color.rgb(156, 220, 254) // Light blue
    private val functionColor = Color.rgb(220, 220, 170)  // Yellow-ish

    // Patterns for different languages
    private val htmlPatterns = mapOf(
        Pattern.compile("</?\\w+[^>]*>") to tagColor,                       // HTML tags
        Pattern.compile("\\s(\\w+)=") to attributeColor,                    // HTML attributes
        Pattern.compile("\"[^\"]*\"") to stringColor,                       // Strings
        Pattern.compile("<!--[\\s\\S]*?-->") to commentColor                // Comments
    )

    private val cssPatterns = mapOf(
        Pattern.compile("\\{[^\\}]*\\}") to Color.WHITE,                    // CSS blocks - Fixed
        Pattern.compile("[.#]\\w+[^\\{]*\\{") to tagColor,                  // Selectors - Fixed
        Pattern.compile("\\b(margin|padding|font|color|background|display|width|height|border|position|top|left|right|bottom|float|clear|text-align|line-height)\\b") to keywordColor, // CSS properties
        Pattern.compile(":\\s*[^;]*") to attributeColor,                    // CSS values
        Pattern.compile("/\\*[\\s\\S]*?\\*/") to commentColor               // Comments
    )

    private val jsPatterns = mapOf(
        Pattern.compile("\\b(var|let|const|function|return|if|else|for|while|do|switch|case|break|continue|new|this|typeof|instanceof)\\b") to keywordColor, // Keywords
        Pattern.compile("\\b(document|window|console|Math|Array|Object|String|Number|Boolean)\\b") to tagColor, // Built-in objects
        Pattern.compile("\\b\\d+(\\.\\d+)?\\b") to numberColor,             // Numbers
        Pattern.compile("\"[^\"]*\"|'[^']*'|`[^`]*`") to stringColor,       // Strings
        Pattern.compile("//[^\\n]*") to commentColor,                       // Single line comments
        Pattern.compile("/\\*[\\s\\S]*?\\*/") to commentColor,              // Multi-line comments
        Pattern.compile("\\b\\w+(?=\\()") to functionColor                  // Function calls
    )

    private val pythonPatterns = mapOf(
        Pattern.compile("\\b(def|class|import|from|as|if|elif|else|for|while|try|except|finally|with|return|yield|break|continue|pass|raise|True|False|None)\\b") to keywordColor, // Keywords
        Pattern.compile("\\b(print|len|range|int|str|list|dict|set|tuple)\\b") to tagColor, // Built-in functions
        Pattern.compile("\\b\\d+(\\.\\d+)?\\b") to numberColor,             // Numbers
        Pattern.compile("\"[^\"]*\"|'[^']*'") to stringColor,               // Strings (simplified)
        Pattern.compile("#[^\\n]*") to commentColor,                        // Comments
        Pattern.compile("\\b\\w+(?=\\()") to functionColor                  // Function calls
    )

    private val phpPatterns = mapOf(
        Pattern.compile("\\b(if|else|elseif|while|do|for|foreach|as|switch|case|break|continue|return|function|class|new|echo|print|include|require)\\b") to keywordColor, // Keywords
        Pattern.compile("\\$\\w+") to attributeColor,                       // Variables
        Pattern.compile("\\b\\d+(\\.\\d+)?\\b") to numberColor,             // Numbers
        Pattern.compile("\"[^\"]*\"|'[^']*'") to stringColor,               // Strings
        Pattern.compile("//[^\\n]*|#[^\\n]*") to commentColor,              // Single line comments
        Pattern.compile("/\\*[\\s\\S]*?\\*/") to commentColor,              // Multi-line comments
        Pattern.compile("\\b\\w+(?=\\()") to functionColor                  // Function calls
    )

    private val javaPatterns = mapOf(
        Pattern.compile("\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|if|implements|import|instanceof|int|interface|long|new|package|private|protected|public|return|static|switch|this|throw|try|void|while)\\b") to keywordColor, // Keywords (simplified)
        Pattern.compile("\\b(true|false|null)\\b") to keywordColor,         // Literals
        Pattern.compile("\\b\\d+(\\.\\d+)?\\b") to numberColor,             // Numbers
        Pattern.compile("\"[^\"]*\"") to stringColor,                       // Strings
        Pattern.compile("//[^\\n]*") to commentColor,                       // Single line comments
        Pattern.compile("/\\*[\\s\\S]*?\\*/") to commentColor,              // Multi-line comments
        Pattern.compile("\\b\\w+(?=\\()") to functionColor                  // Function calls
    )

    fun highlight(editText: EditText, language: String) {
        // Prevent recursive calls
        if (isHighlighting) return
        
        try {
            isHighlighting = true
            
            val text = editText.text.toString()
            val spannable = SpannableStringBuilder(text)
            
            // Clear existing spans
            val spans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
            for (span in spans) {
                spannable.removeSpan(span)
            }
            
            // Apply highlighting based on language
            val patterns = when (language.lowercase()) {
                "html", "htm" -> htmlPatterns
                "css" -> cssPatterns
                "js", "javascript" -> jsPatterns
                "py", "python" -> pythonPatterns
                "php" -> phpPatterns
                "java" -> javaPatterns
                else -> emptyMap() // No highlighting for unknown languages
            }
            
            // Apply patterns
            for ((pattern, color) in patterns) {
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    spannable.setSpan(
                        ForegroundColorSpan(color),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            
            // Save current cursor position
            val selectionStart = editText.selectionStart
            val selectionEnd = editText.selectionEnd
            
            // Set the text without triggering text changed events
            editText.removeTextChangedListener(null) // This removes all watchers
            
            editText.text = spannable
            
            // Restore cursor position
            try {
                val newLength = editText.text.length
                val newSelStart = Math.min(selectionStart, newLength)
                val newSelEnd = Math.min(selectionEnd, newLength)
                editText.setSelection(newSelStart, newSelEnd)
            } catch (e: Exception) {
                // If setting selection fails, just put cursor at the end
                try {
                    editText.setSelection(editText.text.length)
                } catch (e: Exception) {
                    // Ignore if even this fails
                }
            }
            
        } catch (e: Exception) {
            // If anything goes wrong, just show the text without highlighting
            e.printStackTrace()
        } finally {
            isHighlighting = false
        }
    }
}

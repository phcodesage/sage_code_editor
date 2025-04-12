package com.example.sagecodeeditor.util

object LanguageDetector {
    
    fun detectLanguage(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        
        return when (extension) {
            "html", "htm" -> "HTML"
            "css" -> "CSS"
            "js" -> "JavaScript"
            "py" -> "Python"
            "java" -> "Java"
            "php" -> "PHP"
            "json" -> "JSON"
            "txt" -> "Plain Text"
            else -> "Plain Text"
        }
    }
    
    fun getFileIcon(extension: String): Int {
        // Will implement this later with actual drawable resources
        return 0
    }
    
    fun isSupportedLanguage(extension: String): Boolean {
        return when (extension.lowercase()) {
            "html", "htm", "css", "js", "py", "java", "php", "json", "txt" -> true
            else -> false
        }
    }
}

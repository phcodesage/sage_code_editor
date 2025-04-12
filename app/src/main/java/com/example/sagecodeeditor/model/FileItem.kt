package com.example.sagecodeeditor.model

import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val documentFile: DocumentFile? = null,
    val file: File? = null,
    val lastModified: Long = 0,
    val size: Long = 0
) {
    val extension: String
        get() = name.substringAfterLast('.', "")
}

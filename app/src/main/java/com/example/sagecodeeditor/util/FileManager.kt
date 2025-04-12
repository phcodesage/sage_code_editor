package com.example.sagecodeeditor.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.sagecodeeditor.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileManager(private val context: Context) {

    suspend fun listFiles(directoryUri: Uri): List<FileItem> = withContext(Dispatchers.IO) {
        val documentFile = DocumentFile.fromTreeUri(context, directoryUri)
        val result = mutableListOf<FileItem>()
        
        documentFile?.listFiles()?.forEach { file ->
            result.add(
                FileItem(
                    name = file.name ?: "",
                    path = file.uri.toString(),
                    isDirectory = file.isDirectory,
                    documentFile = file,
                    lastModified = file.lastModified(),
                    size = if (file.isFile) file.length() else 0
                )
            )
        }
        
        result.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
    
    suspend fun readTextFile(fileUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                return@withContext inputStream.bufferedReader().use { it.readText() }
            } ?: ""
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext ""
        }
    }
    
    suspend fun writeTextFile(fileUri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                return@withContext true
            } ?: return@withContext false
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext false
        }
    }
    
    suspend fun createFile(parentUri: Uri, fileName: String): Uri? = withContext(Dispatchers.IO) {
        val parent = DocumentFile.fromTreeUri(context, parentUri)
        val mimeType = getMimeType(fileName)
        val file = parent?.createFile(mimeType, fileName)
        file?.uri
    }
    
    suspend fun createDirectory(parentUri: Uri, dirName: String): Uri? = withContext(Dispatchers.IO) {
        val parent = DocumentFile.fromTreeUri(context, parentUri)
        val dir = parent?.createDirectory(dirName)
        dir?.uri
    }
    
    suspend fun deleteFile(fileUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val file = DocumentFile.fromSingleUri(context, fileUri)
        file?.delete() ?: false
    }
    
    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "py" -> "text/x-python"
            "java" -> "text/x-java"
            "php" -> "application/x-php"
            "json" -> "application/json"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
    
    // For working with app's private storage
    suspend fun listInternalFiles(directory: File): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        
        directory.listFiles()?.forEach { file ->
            result.add(
                FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    file = file,
                    lastModified = file.lastModified(),
                    size = if (file.isFile) file.length() else 0
                )
            )
        }
        
        result.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
    
    suspend fun readInternalFile(file: File): String = withContext(Dispatchers.IO) {
        try {
            file.readText()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
    
    suspend fun writeInternalFile(file: File, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(file).use { it.write(content.toByteArray()) }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun createInternalFile(directory: File, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(directory, fileName)
            if (file.createNewFile()) file else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun createInternalDirectory(directory: File, dirName: String): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(directory, dirName)
            if (dir.mkdir()) dir else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun deleteInternalFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            file.delete()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}

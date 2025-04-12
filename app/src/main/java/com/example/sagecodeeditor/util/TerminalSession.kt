package com.example.sagecodeeditor.util

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit

class TerminalSessionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TerminalSessionManager"
    }
    
    private var currentProcess: Process? = null
    private var outputBuffer = StringBuilder()
    
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("/system/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            
            currentProcess = process
            
            // Read the output
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            // Wait for the process to complete
            try {
                // For API level 24+, we'll use a simple waitFor without timeout
                process.waitFor()
            } catch (e: InterruptedException) {
                Log.e(TAG, "Process wait interrupted: ${e.message}")
            }
            
            val result = output.toString()
            outputBuffer.append(command).append("\n").append(result).append("\n")
            
            result
        } catch (e: IOException) {
            Log.e(TAG, "Error executing command: ${e.message}")
            e.printStackTrace()
            "Error: ${e.message}"
        } catch (e: InterruptedException) {
            Log.e(TAG, "Command execution interrupted: ${e.message}")
            e.printStackTrace()
            "Interrupted: ${e.message}"
        }
    }
    
    suspend fun runPythonCode(code: String): String = withContext(Dispatchers.IO) {
        val tempDir = context.getExternalFilesDir("python") ?: return@withContext "Failed to create temp directory"
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        val tempFile = File(tempDir, "script_${UUID.randomUUID()}.py")
        
        try {
            tempFile.writeText(code)
            executeCommand("python ${tempFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to run Python code: ${e.message}")
            e.printStackTrace()
            "Failed to run Python code: ${e.message}"
        }
    }
    
    suspend fun runPhpCode(code: String): String = withContext(Dispatchers.IO) {
        val tempDir = context.getExternalFilesDir("php") ?: return@withContext "Failed to create temp directory"
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        val tempFile = File(tempDir, "script_${UUID.randomUUID()}.php")
        
        try {
            tempFile.writeText(code)
            executeCommand("php ${tempFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to run PHP code: ${e.message}")
            e.printStackTrace()
            "Failed to run PHP code: ${e.message}"
        }
    }
    
    fun getOutputBuffer(): String {
        return outputBuffer.toString()
    }
    
    fun clearOutputBuffer() {
        outputBuffer.clear()
    }
    
    fun closeSession() {
        currentProcess?.destroy()
        currentProcess = null
    }
}

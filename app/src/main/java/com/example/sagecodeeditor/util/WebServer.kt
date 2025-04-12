package com.example.sagecodeeditor.util

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.IOException

class WebServer(private val port: Int = 8080) : NanoHTTPD(port) {
    
    private var htmlContent: String = ""
    private var cssContent: String = ""
    private var jsContent: String = ""
    private var pythonOutput: String = ""
    private var phpOutput: String = ""
    
    companion object {
        private const val TAG = "WebServer"
    }
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d(TAG, "Request received: $uri")
        
        return when {
            uri.endsWith(".css") -> newChunkedResponse(
                Response.Status.OK, "text/css", ByteArrayInputStream(cssContent.toByteArray())
            )
            uri.endsWith(".js") -> newChunkedResponse(
                Response.Status.OK, "application/javascript", ByteArrayInputStream(jsContent.toByteArray())
            )
            uri == "/python" -> newChunkedResponse(
                Response.Status.OK, "text/plain", ByteArrayInputStream(pythonOutput.toByteArray())
            )
            uri == "/php" -> newChunkedResponse(
                Response.Status.OK, "text/plain", ByteArrayInputStream(phpOutput.toByteArray())
            )
            else -> newChunkedResponse(
                Response.Status.OK, "text/html", ByteArrayInputStream(htmlContent.toByteArray())
            )
        }
    }
    
    fun setHtmlContent(content: String) {
        htmlContent = content
    }
    
    fun setCssContent(content: String) {
        cssContent = content
    }
    
    fun setJsContent(content: String) {
        jsContent = content
    }
    
    fun setPythonOutput(output: String) {
        pythonOutput = output
    }
    
    fun setPhpOutput(output: String) {
        phpOutput = output
    }
    
    suspend fun startServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            start(SOCKET_READ_TIMEOUT, false)
            Log.d(TAG, "Server started on port $port")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun stopServer() = withContext(Dispatchers.IO) {
        try {
            stop()
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
            e.printStackTrace()
        }
    }
}

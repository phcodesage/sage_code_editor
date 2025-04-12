package com.example.sagecodeeditor.ui.screens

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.sagecodeeditor.model.FileItem
import com.example.sagecodeeditor.ui.components.LineNumberEditText
import com.example.sagecodeeditor.ui.navigation.Screen
import com.example.sagecodeeditor.util.FileManager
import com.example.sagecodeeditor.util.SyntaxHighlighter
import com.example.sagecodeeditor.util.WebServer
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    navController: NavController,
    filePath: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fileManager = remember { FileManager(context) }
    val webServer = remember { WebServer() }
    val syntaxHighlighter = remember { SyntaxHighlighter() }
    
    var fileContent by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("Untitled.txt") }
    var isServerRunning by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf("") }
    var isNewFile by remember { mutableStateOf(filePath.isEmpty()) }
    var currentLanguage by remember { mutableStateOf("txt") }
    
    // State for HTML project dialog
    var showHtmlProjectDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(filePath) {
        try {
            if (filePath.isEmpty()) {
                // This is a blank editor, set default content
                fileContent = "// Start coding here\n"
                fileName = "Untitled.txt"
                isNewFile = true
            } else {
                val decodedPath = Uri.decode(filePath)
                
                if (decodedPath.startsWith("/")) {
                    // Internal file
                    val file = File(decodedPath)
                    fileName = file.name
                    fileContent = fileManager.readInternalFile(file)
                    isNewFile = false
                } else {
                    // External file via content URI
                    val uri = Uri.parse(decodedPath)
                    fileName = uri.lastPathSegment ?: "Untitled.txt"
                    fileContent = fileManager.readTextFile(uri)
                    isNewFile = false
                }
            }
            
            // Get file extension for syntax highlighting
            currentLanguage = fileName.substringAfterLast('.', "txt").lowercase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            if (isServerRunning) {
                coroutineScope.launch {
                    webServer.stopServer()
                }
            }
        }
    }
    
    // HTML Project Dialog
    if (showHtmlProjectDialog) {
        AlertDialog(
            onDismissRequest = { showHtmlProjectDialog = false },
            title = { Text("Create HTML Project") },
            text = { Text("This will create HTML, CSS, and JavaScript files for your project.") },
            confirmButton = {
                TextButton(onClick = {
                    // Create project files
                    coroutineScope.launch {
                        // Create HTML file (current file)
                        fileName = "index.html"
                        currentLanguage = "html"
                        fileContent = """
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <title>My Web Project</title>
                                <link rel="stylesheet" href="styles.css">
                            </head>
                            <body>
                                <h1>Welcome to My Web Project</h1>
                                <p>This is a starter template for your web project.</p>
                                
                                <script src="script.js"></script>
                            </body>
                            </html>
                        """.trimIndent()
                        
                        // Create CSS file
                        val cssFile = File(context.filesDir, "styles.css")
                        val cssContent = """
                            /* styles.css */
                            body {
                                font-family: Arial, sans-serif;
                                margin: 0;
                                padding: 20px;
                                line-height: 1.6;
                                color: #333;
                            }
                            
                            h1 {
                                color: #0066cc;
                            }
                        """.trimIndent()
                        fileManager.writeInternalFile(cssFile, cssContent)
                        
                        // Create JS file
                        val jsFile = File(context.filesDir, "script.js")
                        val jsContent = """
                            // script.js
                            document.addEventListener('DOMContentLoaded', function() {
                                console.log('Page loaded successfully!');
                                
                                // Your JavaScript code goes here
                            });
                        """.trimIndent()
                        fileManager.writeInternalFile(jsFile, jsContent)
                    }
                    showHtmlProjectDialog = false
                }) {
                    Text("Create Project")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHtmlProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                },
                actions = {
                    if (isNewFile) {
                        Button(onClick = {
                            navController.navigate(Screen.FileExplorer.route)
                        }) {
                            Text("Save As")
                        }
                    } else {
                        Button(onClick = {
                            coroutineScope.launch {
                                if (filePath.startsWith("/")) {
                                    // Internal file
                                    val file = File(filePath)
                                    fileManager.writeInternalFile(file, fileContent)
                                } else {
                                    // External file via content URI
                                    val uri = Uri.parse(filePath)
                                    fileManager.writeTextFile(uri, fileContent)
                                }
                            }
                        }) {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Language selector for new files
            if (isNewFile) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language: ", modifier = Modifier.padding(end = 8.dp))
                    Button(onClick = { 
                        // Show dialog for HTML project creation
                        showHtmlProjectDialog = true
                    }) {
                        Text("HTML")
                    }
                    Button(onClick = { 
                        currentLanguage = "css"
                        fileName = "Untitled.css"
                        if (fileContent.isBlank()) {
                            fileContent = "body {\n    font-family: Arial, sans-serif;\n    margin: 0;\n    padding: 20px;\n}"
                        }
                    }, modifier = Modifier.padding(start = 4.dp)) {
                        Text("CSS")
                    }
                    Button(onClick = { 
                        currentLanguage = "js"
                        fileName = "Untitled.js"
                        if (fileContent.isBlank()) {
                            fileContent = "// JavaScript code\nconsole.log('Hello, world!');"
                        }
                    }, modifier = Modifier.padding(start = 4.dp)) {
                        Text("JS")
                    }
                    Button(onClick = { 
                        currentLanguage = "py"
                        fileName = "Untitled.py"
                        if (fileContent.isBlank()) {
                            fileContent = "# Python code\nprint('Hello, world!')"
                        }
                    }, modifier = Modifier.padding(start = 4.dp)) {
                        Text("PY")
                    }
                }
            }
            
            // Code editor with line numbers
            AndroidView(
                factory = { context ->
                    LineNumberEditText(context).apply {
                        setText(fileContent)
                        
                        // Apply initial syntax highlighting
                        syntaxHighlighter.highlight(getEditText(), currentLanguage)
                        
                        // Listen for text changes
                        getEditText().addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            
                            override fun afterTextChanged(s: Editable?) {
                                // Only update the content, don't apply highlighting here
                                fileContent = s.toString()
                                
                                // Apply syntax highlighting after a short delay
                                getEditText().postDelayed({
                                    syntaxHighlighter.highlight(getEditText(), currentLanguage)
                                }, 300)
                            }
                        })
                    }
                },
                update = { lineNumberEditText ->
                    // Only update if the content has actually changed from outside
                    if (lineNumberEditText.getText().toString() != fileContent) {
                        lineNumberEditText.setText(fileContent)
                        
                        // Apply syntax highlighting after content update
                        lineNumberEditText.getEditText().postDelayed({
                            syntaxHighlighter.highlight(lineNumberEditText.getEditText(), currentLanguage)
                        }, 100)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.FileExplorer.route)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Text("Files")
                }
                
                Button(
                    onClick = {
                        navController.navigate(Screen.Terminal.route)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text("Terminal")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (!isServerRunning) {
                                when (currentLanguage) {
                                    "html", "htm" -> webServer.setHtmlContent(fileContent)
                                    "css" -> webServer.setCssContent(fileContent)
                                    "js" -> webServer.setJsContent(fileContent)
                                }
                                
                                if (webServer.startServer()) {
                                    isServerRunning = true
                                    serverUrl = "http://localhost:8080"
                                    navController.navigate(Screen.LivePreview.route)
                                }
                            } else {
                                webServer.stopServer()
                                isServerRunning = false
                                serverUrl = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                ) {
                    Text("Preview")
                }
            }
        }
    }
}

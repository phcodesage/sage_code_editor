package com.example.sagecodeeditor.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.sagecodeeditor.model.FileItem
import com.example.sagecodeeditor.ui.navigation.Screen
import com.example.sagecodeeditor.util.FileManager
import com.example.sagecodeeditor.util.LanguageDetector
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fileManager = remember { FileManager(context) }
    
    var currentFiles by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var currentDirectory by remember { mutableStateOf<File?>(context.getExternalFilesDir(null)) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val files = fileManager.listFiles(it)
                currentFiles = files
            }
        }
    }
    
    LaunchedEffect(currentDirectory) {
        currentDirectory?.let {
            if (it.exists() && it.isDirectory) {
                val files = fileManager.listInternalFiles(it)
                currentFiles = files
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Explorer") },
                navigationIcon = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewFileDialog = true }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = { directoryPicker.launch(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Select Directory")
            }
            
            Button(
                onClick = { showNewFolderDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Create New Folder")
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(currentFiles) { fileItem ->
                    FileItemRow(
                        fileItem = fileItem,
                        onClick = {
                            if (fileItem.isDirectory) {
                                fileItem.file?.let {
                                    currentDirectory = it
                                }
                            } else {
                                // Open file in code editor
                                val encodedPath = Uri.encode(fileItem.path)
                                navController.navigate("${Screen.CodeEditor.route}/$encodedPath")
                            }
                        }
                    )
                }
            }
        }
        
        if (showNewFileDialog) {
            AlertDialog(
                onDismissRequest = { showNewFileDialog = false },
                title = { Text("Create New File") },
                text = {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                currentDirectory?.let { dir ->
                                    fileManager.createInternalFile(dir, newFileName)?.let {
                                        // Refresh file list
                                        currentFiles = fileManager.listInternalFiles(dir)
                                    }
                                }
                            }
                            newFileName = ""
                            showNewFileDialog = false
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showNewFileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                title = { Text("Create New Folder") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                currentDirectory?.let { dir ->
                                    fileManager.createInternalDirectory(dir, newFolderName)?.let {
                                        // Refresh file list
                                        currentFiles = fileManager.listInternalFiles(dir)
                                    }
                                }
                            }
                            newFolderName = ""
                            showNewFolderDialog = false
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    Button(onClick = { showNewFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FileItemRow(fileItem: FileItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (fileItem.isDirectory) "📁" else "📄",
            modifier = Modifier.padding(end = 16.dp)
        )
        
        Column {
            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = if (fileItem.isDirectory) "Directory" else "${fileItem.size} bytes",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

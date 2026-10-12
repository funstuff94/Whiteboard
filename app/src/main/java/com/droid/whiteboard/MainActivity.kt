package com.droid.whiteboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.droid.whiteboard.data.FileWhiteboardRepository
import com.droid.whiteboard.theme.WhiteBoardTheme
import com.droid.whiteboard.ui.WhiteboardScreen
import com.droid.whiteboard.ui.WhiteboardViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: WhiteboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val storageDir = File(filesDir, "whiteboards")
                val repository = FileWhiteboardRepository(storageDir)
                return WhiteboardViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WhiteBoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WhiteboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}

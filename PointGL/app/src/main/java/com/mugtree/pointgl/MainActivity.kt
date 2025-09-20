package com.mugtree.pointgl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mugtree.pointgl.opengl.MyGLSurfaceView
import com.mugtree.pointgl.pcl.MugPointCloud
import com.mugtree.pointgl.ui.theme.PointGLTheme

class MainActivity : ComponentActivity() {
    private lateinit var glView: MyGLSurfaceView
    private var mugPointCloud = MugPointCloud()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         glView = MyGLSurfaceView(this)

        setContent {
            PointGLTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Button(onClick = { openFilePicker() }) {
                            Text("Select PLY File")
                        }
                        AndroidView(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            factory = { context ->
                                glView.apply {  }
                            },
                            update = { view ->

                            }
                        )
                    }
                }
            }
        }
    }

    // File picker launcher
    private val filePicker =
        this.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                // Open stream from URI
                Thread {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        mugPointCloud.loadPLY(this, inputStream)
                    }
                    if (mugPointCloud.mugPointCloudData != null) {
                        Handler(Looper.getMainLooper()).post {
                            glView.setPointCloud(mugPointCloud.mugPointCloudData!!)
                        }
                    }
                }.start()
            }
        }

    private fun openFilePicker() {
        filePicker.launch(arrayOf("*/*")) // or restrict to "model/*"
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PointGLTheme {
        Greeting("Android")
    }
}
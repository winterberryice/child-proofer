package com.kacpersledz.child_proofer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kacpersledz.child_proofer.ui.theme.ChildprooferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChildprooferTheme {
                PermissionCheckScreen()
            }
        }
    }

}

@Composable
fun PermissionCheckScreen() {
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        val permissionState = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }


    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                val hasPermission = hasWriteSecureSettingsPermission(context)
                if (hasPermission) {
                    Toast.makeText(context, "Permission is GRANTED!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Permission is DENIED. Please use ADB.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) {
                Text("Check Permission Status")
            }
        }
    }
}
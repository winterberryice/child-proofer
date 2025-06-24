package com.kacpersledz.child_proofer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return Settings.Secure.canWrite(context)
    }
}

@Composable
fun PermissionCheckScreen() {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val hasPermission = (context as MainActivity).hasWriteSecureSettingsPermission(context)
                    if (hasPermission) {
                        Toast.makeText(context, "Permission is GRANTED!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Permission is DENIED. Please use ADB.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Feature not available for this API level", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Check Permission Status")
            }
        }
    }
}
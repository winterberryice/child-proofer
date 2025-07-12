package com.kacpersledz.child_proofer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kacpersledz.child_proofer.ui.theme.ChildprooferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChildprooferTheme {
                // We pass the helper functions down to the composable
                PermissionCheckScreen(
                    hasPermission = { hasWriteSecureSettingsPermission(this) },
                    isTapEnabled = { isTapGestureEnabled(this) },
                    isLiftEnabled = { isLiftToWakeEnabled(this) },
                    setSettings = { enabled ->
                        setTapGestureEnabled(this, enabled)
                        setLiftToWakeEnabled(this, enabled)
                    }
                )
            }
        }
    }

    // --- HELPER FUNCTIONS ---
    // It's better practice to keep these helper functions in the Activity
    // so the Composable stays focused on UI.

    private fun isSamsungDevice(): Boolean {
        return android.os.Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    private fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isTapGestureEnabled(context: Context): Boolean {
        return if (isSamsungDevice()) {
            Settings.Secure.getInt(context.contentResolver, "double_tab_to_wake_up", 0) == 1
        } else {
            Settings.Secure.getInt(context.contentResolver, "doze_tap_gesture", 0) == 1
        }
    }

    private fun setTapGestureEnabled(context: Context, enabled: Boolean) {
        if (isSamsungDevice()) {
            Settings.Secure.putInt(context.contentResolver, "double_tab_to_wake_up", if (enabled) 1 else 0)
        } else {
            Settings.Secure.putInt(context.contentResolver, "doze_tap_gesture", if (enabled) 1 else 0)
        }
    }

    private fun isLiftToWakeEnabled(context: Context): Boolean {
        return if (isSamsungDevice()) {
            Settings.Secure.getInt(context.contentResolver, "lift_to_wake", 0) == 1
        } else {
            Settings.Secure.getInt(context.contentResolver, "doze_pulse_on_pick_up", 0) == 1
        }
    }

    private fun setLiftToWakeEnabled(context: Context, enabled: Boolean) {
        if (isSamsungDevice()) {
            Settings.Secure.putInt(context.contentResolver, "lift_to_wake", if (enabled) 1 else 0)
        } else {
            Settings.Secure.putInt(
                context.contentResolver,
                "doze_pulse_on_pick_up",
                if (enabled) 1 else 0
            )
        }
    }
}

@Composable
fun PermissionCheckScreen(
    // Define lambdas for all the actions we need
    hasPermission: () -> Boolean,
    isTapEnabled: () -> Boolean,
    isLiftEnabled: () -> Boolean,
    setSettings: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // --- STATE MANAGEMENT ---
    val tapStatus = remember { mutableStateOf(isTapEnabled()) }
    val liftStatus = remember { mutableStateOf(isLiftEnabled()) }

    // --- NEW: State for the Master Child Proofing Switch ---
    // Child proofing is considered "ON" if the underlying settings are "OFF".
    // We can initialize its state by checking if the tap gesture is disabled.
    val childProofingStatus = remember { mutableStateOf(!isTapEnabled()) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. The original permission check button (unchanged)
        Button(onClick = {
            val message = if (hasPermission()) "Permission is GRANTED!" else "Permission is DENIED."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }) {
            Text("Check Permission Status")
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 2. The status labels (unchanged)
        Text(
            text = "Tap to Wake: ${if (tapStatus.value) "Enabled" else "Disabled"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lift to Wake: ${if (liftStatus.value) "Enabled" else "Disabled"}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. REPLACED: The old buttons are replaced with this new master toggle ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Child Proofing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(16.dp))
            androidx.compose.material3.Switch(
                checked = childProofingStatus.value,
                onCheckedChange = { isProofingOn ->
                    // This is the new state of the "Child Proofing" switch
                    childProofingStatus.value = isProofingOn

                    // Invert the logic: If proofing is ON, the settings should be OFF.
                    val areSettingsEnabled = !isProofingOn
                    setSettings(areSettingsEnabled)

                    // Update the status labels' state to redraw the UI
                    tapStatus.value = areSettingsEnabled
                    liftStatus.value = areSettingsEnabled

                    // Show a confirmation toast
                    val toastMessage = if (isProofingOn) "Child Proofing ON" else "Child Proofing OFF"
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

package com.kacpersledz.child_proofer

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.RemoteViews
import android.app.PendingIntent

private const val ACTION_WIDGET_TOGGLE = "com.kacpersledz.child_proofer.ACTION_WIDGET_TOGGLE"

/**
 * Implementation of App Widget functionality.
 */
class ChildProoferWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    // Inside ChildProoferWidgetProvider.kt

    override fun onReceive(context: Context, intent: Intent) {
        // First, let the super class handle standard widget actions
        super.onReceive(context, intent)

        // Check if this is our custom toggle action
        if (intent.action == ACTION_WIDGET_TOGGLE) {
            // --- 1. Perform the toggle logic ---
            val isCurrentlyOn =
                Settings.Secure.getInt(context.contentResolver, "doze_tap_gesture", 0) != 1
            val newSettingsEnabled =
                isCurrentlyOn // If proofing is ON, new settings should be OFF (enabled=false)

            // We reuse the same logic from MainActivity again
            Settings.Secure.putInt(
                context.contentResolver,
                "doze_tap_gesture",
                if (newSettingsEnabled) 1 else 0
            )
            Settings.Secure.putInt(
                context.contentResolver,
                "doze_pulse_on_pick_up",
                if (newSettingsEnabled) 1 else 0
            )

            // --- 2. Force the widget to update its appearance ---
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}


// Inside ChildProoferWidgetProvider.kt

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // --- 1. Get the current state ---
    // We reuse the same logic from your MainActivity!
    val isChildProofingOn =
        Settings.Secure.getInt(context.contentResolver, "doze_tap_gesture", 0) != 1

    // --- 2. Create RemoteViews ---
    val views = RemoteViews(context.packageName, R.layout.child_proofer_widget_provider)

    // --- 3. Update the UI based on state ---
    val iconResId = if (isChildProofingOn) R.drawable.ic_widget_on else R.drawable.ic_widget_off
    views.setImageViewResource(R.id.widget_toggle_button, iconResId)

    // --- 4. Create the PendingIntent for the click ---
    val intent = Intent(context, ChildProoferWidgetProvider::class.java).apply {
        action = ACTION_WIDGET_TOGGLE
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId, // Use the widgetId as the request code to make it unique
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // --- 5. Attach the PendingIntent to the button ---
    views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent)

    // --- 6. Tell the AppWidgetManager to update the widget ---
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

package com.example.campusconnectapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object WebUtils {
    /**
     * Opens a web URL instantly using AndroidX Custom Tabs.
     * Custom Tabs pre-warms the browser engine and stays in app context,
     * drastically reducing redirection delay and eliminating cold-start lag.
     */
    fun openUrlInstantly(context: Context, url: String) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            val uri = Uri.parse(formattedUrl)

            // Custom Tabs Builder for instant, fast loading
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setInstantAppsEnabled(true)
                .build()

            // Optimize intent flags for instant focus
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            // Fallback to fast default browser intent
            try {
                val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else url
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (fallbackEx: Exception) {
                Toast.makeText(context, "Unable to open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.muslimlife.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "नमाज़"

        showPrayerNotification(context, prayerName)
    }
}

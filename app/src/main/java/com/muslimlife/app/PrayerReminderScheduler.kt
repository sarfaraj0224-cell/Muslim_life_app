

package com.muslimlife.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object PrayerReminderScheduler {

    fun schedule(
        context: Context,
        prayerName: String,
        prayerTime: String
    ) {
        try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val parsed = format.parse(prayerTime) ?: return

            val calendar = Calendar.getInstance().apply {
                time = parsed

                val now = Calendar.getInstance()
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(
                context,
                PrayerNotificationReceiver::class.java
            ).apply {
                putExtra("prayer_name", prayerName)
                putExtra("prayer_time", prayerTime)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayerName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancel(context: Context, prayerName: String) {
        val intent = Intent(
            context,
            PrayerNotificationReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
    }
}

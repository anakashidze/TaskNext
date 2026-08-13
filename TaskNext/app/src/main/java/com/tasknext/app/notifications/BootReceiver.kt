package com.tasknext.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Exact alarms are cleared on reboot. This receiver exists so the manifest
 * registration is valid; actual rescheduling happens in MainActivity.onCreate,
 * which re-registers all enabled reminders from the database every time the
 * app is opened. For a production app you'd move that rescheduling logic here
 * instead, reading directly from Room.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // No-op by design; see MainActivity for reschedule-on-launch logic.
    }
}

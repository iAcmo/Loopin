package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID = "loopin_messages_channel"
    const val CHANNEL_NAME = "Loopin Direct Messages"
    const val CHANNEL_DESC = "Notifications for incoming direct messages, group chats, and replies"

    const val ACTION_OPEN_ROOM = "com.example.ACTION_OPEN_ROOM"
    const val EXTRA_ROOM_ID = "extra_room_id"
    const val EXTRA_ROOM_NAME = "extra_room_name"

    private const val NOTIFICATION_GROUP = "com.example.loopin.DIRECT_MESSAGES"

    /**
     * Initializes the high-priority notification channel for Direct Messages on Android 8.0+.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.parseColor("#7E74F1")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Sends a device status bar / heads-up notification for an incoming chat message.
     * Clicking the notification opens the chat room directly in MainActivity.
     */
    fun showMessageNotification(
        context: Context,
        roomId: String,
        title: String,
        messageText: String,
        senderName: String,
        isGroup: Boolean = false
    ) {
        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_ROOM
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_ROOM_NAME, title)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerTitle = if (isGroup) {
            if (senderName.isNotBlank() && !title.contains(senderName)) "$senderName in $title" else title
        } else {
            senderName.ifBlank { title.ifBlank { "New Message" } }
        }

        val displayBody = messageText.ifBlank { "Sent an attachment" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_loopin_logo)
            .setContentTitle(headerTitle)
            .setContentText(displayBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(headerTitle)
                    .bigText(displayBody)
            )
            .setColor(Color.parseColor("#7E74F1"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setGroup(NOTIFICATION_GROUP)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            val notificationId = (roomId.hashCode() and 0x7FFFFFFF)
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission might be denied on Android 13+
        }
    }

    /**
     * Dispatches an immediate test notification so the user can verify device notification delivery.
     */
    fun sendTestNotification(context: Context) {
        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_ROOM
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROOM_ID, "test_demo_room")
            putExtra(EXTRA_ROOM_NAME, "Loopin Support")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_loopin_logo)
            .setContentTitle("Loopin Team")
            .setContentText("✨ Push notifications are active! Tap to open Loopin DMs.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("✨ Push notifications are active! You will now receive instant alerts whenever someone sends you a message.")
            )
            .setColor(Color.parseColor("#7E74F1"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            notificationManager.notify(99999, notification)
        } catch (_: SecurityException) {}
    }
}

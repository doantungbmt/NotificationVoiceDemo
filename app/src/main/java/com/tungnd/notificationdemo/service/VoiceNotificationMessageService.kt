package com.tungnd.notificationdemo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tungnd.notificationdemo.R
import com.tungnd.notificationdemo.activity.MainActivity
import java.util.Locale

class VoiceNotificationMessageService : FirebaseMessagingService() {

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        remoteMessage.notification?.let {
//            val messageBody = it.body ?: return
//            Log.d("FCM", "Thông báo nhận được: $messageBody")
//
//            // Gọi hàm phát giọng nói khi nhận thông báo
//            speakText(messageBody)
//        }
//    }
//
//    private fun speakText(message: String) {
//        val intent = Intent(this, TTSForegroundService::class.java).apply {
//            putExtra("message", message)
//        }
//        startService(intent) // Chạy Service phát giọng nói
//    }

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
//    }

//    private fun sendNotification(title: String?, body: String?) {
//        val intent = Intent(this, Class.forName("com.tungnd.notificationdemo.activity.SecondActivity"))
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(this, "default_channel_id")
//            .setSmallIcon(R.drawable.ic_vietcombank)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "default_channel_id",
//                "Default Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        notificationManager.notify(0, notificationBuilder.build())
//    }

    private lateinit var tts: TextToSpeech
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            val channelId = getString(R.string.default_notification_channel_id)

            val notificationBuilder = NotificationCompat.Builder(this.applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_vietcombank)
                .setContentTitle(remoteMessage.data.get("balance_change_noti"))
                .setContentText(remoteMessage.notification?.body)
                .setAutoCancel(true)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0, notificationBuilder.build())
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        remoteMessage.data.let {
            val str = remoteMessage.data.get("balance_change_noti")
            Log.d("bodyyyyy", "onMessageReceived: $str" )
            textToSpeech(remoteMessage.data.get("balance_change_noti").toString())
        }
    }

    private fun textToSpeech(text: String) {
        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale("vi"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported or missing data.")
                } else {
                    // Speak the text
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } else {
                Log.e("TTS", "Initialization failed.")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shutdown TTS when done to release resources
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
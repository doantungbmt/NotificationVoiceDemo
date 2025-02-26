package com.tungnd.notificationdemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tungnd.notificationdemo.R
import java.util.Locale

class TTSForegroundService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var message: String? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        message = intent?.getStringExtra("message")

        // Hiển thị notification để giữ Service chạy nền
        createNotification()

        // Chờ TextToSpeech khởi tạo xong rồi mới đọc thông báo
        return START_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("vi")

            val result = tts?.isLanguageAvailable(Locale("vi"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ngôn ngữ không hỗ trợ")
                return
            }

            // Gọi speakOut ngay cả khi ứng dụng không mở
            message?.let {
                speakOut(it)
            }
        } else {
            Log.e("TTS", "TTS không khởi tạo được!")
        }
    }

    private fun speakOut(text: String) {
        Log.d("TTS", "Đọc: $text")
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            Log.e("TTS", "Lỗi khi phát giọng nói!")
        }
    }

    private fun createNotification() {
        val channelId = "TTS_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Text To Speech", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ứng dụng đang đọc thông báo")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification) // Giữ Service chạy nền
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

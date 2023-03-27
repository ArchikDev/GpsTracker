package com.archik.gpstracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.archik.gpstracker.MainActivity
import com.archik.gpstracker.R

class LocationService: Service() {
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  // Если память перегружена , то сервис перезапустится(Все переменные очиститься)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startNotify()

    return START_STICKY
  }

  override fun onCreate() {
    super.onCreate()
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  private fun startNotify() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nChannel = NotificationChannel(
        CHANNEL_ID,
        "Location Service",
        NotificationManager.IMPORTANCE_DEFAULT
      )

      val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
      nManager.createNotificationChannel(nChannel)
    }

    val nIntent = Intent(this, MainActivity::class.java)
    val pIntent = PendingIntent.getActivity(this, 10, nIntent, 0)

    val notify = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_circle)
      .setContentTitle("Tracker running!")
      .setContentIntent(pIntent)
      .build()

    startForeground(99, notify)
  }

  companion object {
    const val CHANNEL_ID = "channel_1"
  }
}
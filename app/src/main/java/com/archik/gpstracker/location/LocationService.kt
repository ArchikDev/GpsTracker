package com.archik.gpstracker.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.archik.gpstracker.MainActivity
import com.archik.gpstracker.R
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import org.osmdroid.util.GeoPoint

// Service - сохраняется даже после закрытия приложения, если его не остановили.

class LocationService: Service() {

  private var distance = 0.0f
  private var lastLocation: Location? = null
  // Дистанция(сколько прошли)
  private lateinit var locProvider: FusedLocationProviderClient
  private lateinit var locRequest: LocationRequest
  private lateinit var geoPointsList: ArrayList<GeoPoint>

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  // Если память перегружена , то сервис перезапустится(Все переменные очиститься)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startNotify()

    startLocationUpdates()

    isRunning = true

    return START_STICKY
  }

  override fun onCreate() {
    super.onCreate()

    geoPointsList = ArrayList()

    initLocation()
  }

  override fun onDestroy() {
    super.onDestroy()

    isRunning = false

    locProvider.removeLocationUpdates(locCallBack)
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

  // Дистанция(сколько прошли)
  private fun initLocation() {
    locRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 5000)
      .setMinUpdateIntervalMillis(5000)
      .setMaxUpdateDelayMillis(1000)
      .build()

    locProvider = LocationServices.getFusedLocationProviderClient(baseContext)

  }

  // Дистанция(сколько прошли)
  private val locCallBack = object : LocationCallback() {

    override fun onLocationResult(lResult: LocationResult) {
      super.onLocationResult(lResult)

      val currentLocation = lResult.lastLocation

      if (lastLocation != null && currentLocation != null) {
        if (currentLocation.speed > 0.2) {
          distance += currentLocation.let { lastLocation?.distanceTo(it) } ?: 0.0f
          geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))

          val locModel = LocationModel(
            currentLocation.speed,
            distance,
            geoPointsList
          )

          sendLocData(locModel)
        }
      }

      lastLocation = currentLocation
    }
  }

  private fun sendLocData(locModel: LocationModel) {
    val i = Intent(LOC_MODEL_INTENT)

    i.putExtra(LOC_MODEL_INTENT, locModel)

    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
  }

  // Дистанция(сколько прошли)
  private fun startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) != PackageManager.PERMISSION_GRANTED
    ) return


    locProvider.requestLocationUpdates(
      locRequest,
      locCallBack,
      Looper.myLooper())
  }

  companion object {
    const val CHANNEL_ID = "channel_1"
    const val LOC_MODEL_INTENT = "loc_model_intent"
    var isRunning = false
    var startTime = 0L
  }
}
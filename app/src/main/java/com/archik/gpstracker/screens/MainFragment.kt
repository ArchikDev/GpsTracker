package com.archik.gpstracker.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.archik.gpstracker.MainApp
import com.archik.gpstracker.MainViewModel
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.FragmentMainBinding
import com.archik.gpstracker.db.TrackItem
import com.archik.gpstracker.location.LocationModel
import com.archik.gpstracker.location.LocationService
import com.archik.gpstracker.utils.DialogManager
import com.archik.gpstracker.utils.TimeUtils
import com.archik.gpstracker.utils.checkPermission
import com.archik.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MainFragment : Fragment() {

  private var locationModel: LocationModel? = null
  private var pl: Polyline? = null
  private var isServiceRunning = true
  private var firstStart = true
  private var timer: Timer? = null
  private var startTime = 0L
  private lateinit var mLocOverlay: MyLocationNewOverlay
  private lateinit var binding: FragmentMainBinding
  private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
  private val model: MainViewModel by activityViewModels {
    MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Настройка до инициализации карты
    settingsOsm()

    binding = FragmentMainBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    registerPermissions()
    setOnClicks()
    checkServiceState()
    updateTime()
    registerLocReceiver()
    locationUpdates()
  }

  private fun setOnClicks() = with(binding) {
    val listener = onClicks()

    fStartStop.setOnClickListener(listener)
    fCenter.setOnClickListener(listener)
  }

  private fun onClicks(): View.OnClickListener {
    return View.OnClickListener {
      when(it.id) {
        R.id.fStartStop -> startStopService()
        R.id.fCenter -> centerLocation()
      }
    }
  }

  private fun centerLocation() {
    binding.map.controller.animateTo(mLocOverlay.myLocation )
    mLocOverlay.enableFollowLocation()
  }

  private fun locationUpdates() = with(binding) {
    model.locationUpdates.observe(viewLifecycleOwner) {
      val distance = "Distance: ${String.format("%.1f", it.distance)} m"
      val velocity = "Velocity: ${String.format("%.1f", 3.6f * it.velocity)} km/h"
      val aVelocity = "Average Velocity: ${getAverageSpeed(it.distance)} km/h"

      tvDistance.text = distance
      tvVelocity.text = velocity
      tvAverage.text = aVelocity

      locationModel = it

      updatePolyline(it.geoPointsList)
    }
  }

  private fun getAverageSpeed(distance: Float): String {
    return String.format("%.1f", 3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f)))
  }

  private fun getCurrentTime(): String {
    return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
  }

  private fun geoPointsToString(list: List<GeoPoint>): String {
    val sb = java.lang.StringBuilder()

    list.forEach {
      sb.append("${it.latitude}, ${it.longitude}/")
    }

    return sb.toString()
  }

  private fun updateTime() {
    model.timeDate.observe(viewLifecycleOwner) {
      binding.tvTime.text = it
    }
  }

  private fun startTimer() {
    timer?.cancel()

    timer = Timer()
    startTime = LocationService.startTime
    timer?.schedule(object : TimerTask() {
      override fun run() {
        activity?.runOnUiThread {
          model.timeDate.value = getCurrentTime()
        }
      }
    }, 1, 1)
  }

  private fun startStopService() {
    if (!isServiceRunning) {
      startLocService()
    } else {
      activity?.stopService(Intent(activity, LocationService::class.java))

      binding.fStartStop.setImageResource(R.drawable.ic_play)

      timer?.cancel()

      val track = getTrackItem()

      DialogManager.showSaveDialog(requireContext(),
        track,
        object : DialogManager.Listener {
          override fun onClick() {
            showToast("Saved!")
            model.insertTrack(track)
          }
      })
    }

    isServiceRunning = !isServiceRunning
  }

  private fun getTrackItem(): TrackItem {
    return TrackItem(
      null,
      getCurrentTime(),
      TimeUtils.getDate(),
      String.format("%.1f", locationModel?.distance?.div(1000) ?: 0),
      getAverageSpeed(locationModel?.distance ?: 0.0f),
      geoPointsToString(locationModel?.geoPointsList ?: listOf())
    )

  }

  private fun startLocService() {
    // Запуск сервиса
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      activity?.startForegroundService(Intent(activity, LocationService::class.java))
    } else {
      activity?.startService(Intent(activity, LocationService::class.java))
    }

    binding.fStartStop.setImageResource(R.drawable.ic_stop)

    LocationService.startTime = System.currentTimeMillis()

    startTimer()
  }

  private fun checkServiceState() {
    isServiceRunning = LocationService.isRunning

    if (isServiceRunning) {
      binding.fStartStop.setImageResource(R.drawable.ic_stop)
      startTimer()
    }
  }

  override fun onResume() {
    super.onResume()

    checkLocPermission()
  }

  private fun settingsOsm() {
    Configuration.getInstance().load(
      activity as AppCompatActivity,
      activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
    )
    Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
  }

  private fun initOsm() = with(binding) {
    pl = Polyline()
    pl?.outlinePaint?.color = Color.parseColor(
      PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("color_key", "#000000")
    )

    map.controller.setZoom(20.0)

    val mLocProvider = GpsMyLocationProvider(activity)
    // создаём слой поверх карты
    mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
    mLocOverlay.enableMyLocation() // моё местоположение
    mLocOverlay.enableFollowLocation() // следить за местоположением
    // После того как определилось местположение
    mLocOverlay.runOnFirstFix {
      map.overlays.clear()
      map.overlays.add(mLocOverlay) // Добавляем наш слой
      map.overlays.add(pl) // Добавляем слой Polyline
    }

  }

  private fun registerPermissions() {
    pLauncher = registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()) {

      if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
        initOsm()
        checkLocationEnabled()
      } else {
        showToast("Вы не дали разрешения на использование местоположения")
      }
    }
  }

  private fun checkLocPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      checkPermissionAfter10()
    } else {
      checkPermissionBefore10()
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun checkPermissionAfter10() {
    if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
      && checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    ) {
      initOsm()
      checkLocationEnabled()
    } else {
      pLauncher.launch(arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
      ))
    }
  }

  private fun checkPermissionBefore10() {
    if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
      initOsm()
      checkLocationEnabled()
    } else {
      pLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
    }
  }

  private fun checkLocationEnabled() {
    val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    if (!isEnabled) {
      DialogManager.showLocEnableDialog(
        activity as AppCompatActivity,
        object : DialogManager.Listener {
          override fun onClick() {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
          }
        }
      )
    }
  }

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == LocationService.LOC_MODEL_INTENT) {
        val locModel = intent.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel

        model.locationUpdates.value = locModel
      }
    }
  }

  private fun registerLocReceiver() {
    val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)

    LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiver, locFilter)
  }

  private fun addPoint(list: List<GeoPoint>) {
    pl?.addPoint(list[list.size - 1])
  }

  private fun fillPolyline(list: List<GeoPoint>) {
    list.forEach {
      pl?.addPoint(it)
    }
  }

  private fun updatePolyline(list: List<GeoPoint>) {
    if (list.size > 1 && firstStart) {
      fillPolyline(list)
      firstStart = false
    } else {
      addPoint(list)
    }
  }

  // Когда отключается от активити, т.е. закрывается
  override fun onDetach() {
    super.onDetach()

    LocalBroadcastManager.getInstance(activity as AppCompatActivity)
      .unregisterReceiver(receiver)
  }

  companion object {
    @JvmStatic
    fun newInstance() = MainFragment()
  }
}
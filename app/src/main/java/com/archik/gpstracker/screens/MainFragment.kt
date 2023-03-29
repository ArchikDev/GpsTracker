package com.archik.gpstracker.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.FragmentMainBinding
import com.archik.gpstracker.location.LocationModel
import com.archik.gpstracker.location.LocationService
import com.archik.gpstracker.utils.DialogManager
import com.archik.gpstracker.utils.TimeUtils
import com.archik.gpstracker.utils.checkPermission
import com.archik.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MainFragment : Fragment() {

  private var isServiceRunning = true
  private var timer: Timer? = null
  private var startTime = 0L
  private var timeDate = MutableLiveData<String>()
  private lateinit var binding: FragmentMainBinding
  private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

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
  }

  private fun setOnClicks() = with(binding) {
    val listener = onClicks()

    fStartStop.setOnClickListener(listener)
  }

  private fun onClicks(): View.OnClickListener {
    return View.OnClickListener {
      when(it.id) {
        R.id.fStartStop -> startStopService()
      }
    }
  }

  private fun getCurrentTime(): String {
    return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
  }

  private fun updateTime() {
    timeDate.observe(viewLifecycleOwner) {
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
          timeDate.value = getCurrentTime()
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
    }

    isServiceRunning = !isServiceRunning
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
    map.controller.setZoom(20.0)

    val mLocProvider = GpsMyLocationProvider(activity)
    // создаём слой поверх карты
    val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
    mLocOverlay.enableMyLocation() // моё местоположение
    mLocOverlay.enableFollowLocation() // следить за местоположением
    // После того как определилось местположение
    mLocOverlay.runOnFirstFix {
      map.overlays.clear()
      map.overlays.add(mLocOverlay) // Добавляем наш слой
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
      }
    }
  }

  private fun registerLocReceiver() {
    val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)

    LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiver, locFilter)
  }

  companion object {
    @JvmStatic
    fun newInstance() = MainFragment()
  }
}
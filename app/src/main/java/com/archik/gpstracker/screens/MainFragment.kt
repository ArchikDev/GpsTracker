package com.archik.gpstracker.screens

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.archik.gpstracker.databinding.FragmentMainBinding
import com.archik.gpstracker.utils.checkPermission
import com.archik.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {

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
    } else {
      pLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
    }
  }

  companion object {
    @JvmStatic
    fun newInstance() = MainFragment()
  }
}
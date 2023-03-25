package com.archik.gpstracker.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.FragmentMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {

  private lateinit var binding: FragmentMainBinding

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

    initOsm()
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

  companion object {
    @JvmStatic
    fun newInstance() = MainFragment()
  }
}
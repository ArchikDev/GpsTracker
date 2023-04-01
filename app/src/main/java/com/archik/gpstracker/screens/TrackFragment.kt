package com.archik.gpstracker.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.archik.gpstracker.MainApp
import com.archik.gpstracker.MainViewModel
import com.archik.gpstracker.databinding.FragmentTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

class TrackFragment : Fragment() {
  private lateinit var binding: FragmentTrackBinding

  private val model: MainViewModel by activityViewModels {
    MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Настройка до инициализации карты
    settingsOsm()

    binding = FragmentTrackBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    getTrack()
  }

  private fun getTrack() = with(binding) {
    model.currentTrack.observe(viewLifecycleOwner) {
      val speed = "${it.speed} km/h"
      val time = "${it.time} s"
      val distance = "${it.distance} km"

      tvDate.text = it.date
      tvTime.text = time
      tvAverage.text = speed
      tvDistance.text = distance

      val polyline = getPolyline(it.geoPoints)

      map.overlays.add(polyline)

      goToStartPosition(polyline.actualPoints[0])
    }
  }

  private fun goToStartPosition(startPosition: GeoPoint) {
    binding.map.controller.zoomTo(18.0)
    binding.map.controller.animateTo(startPosition)
  }

  private fun getPolyline(geoPoints: String): Polyline {
    val polyline = Polyline()
    val list = geoPoints.split("/")

    list.forEach {
      if (it.isEmpty()) return@forEach

      val points = it.split(",")

      polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
    }

    return polyline
  }

  private fun settingsOsm() {
    Configuration.getInstance().load(
      activity as AppCompatActivity,
      activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
    )
    Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
  }

  companion object {
    @JvmStatic
    fun newInstance() = TrackFragment()
  }
}
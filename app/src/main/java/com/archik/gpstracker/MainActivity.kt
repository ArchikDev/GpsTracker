package com.archik.gpstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.archik.gpstracker.databinding.ActivityMainBinding
import com.archik.gpstracker.screens.MainFragment
import com.archik.gpstracker.screens.SettingsPreferenceFragment
import com.archik.gpstracker.screens.TracksFragment
import com.archik.gpstracker.utils.openFragment

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    startInit()
  }

  private fun startInit() {
    onBtnNavClick()
    openFragment(MainFragment.newInstance())
  }

  private fun onBtnNavClick() {
    binding.btnNav.setOnItemSelectedListener {
      when(it.itemId) {
        R.id.id_home -> openFragment(MainFragment.newInstance())
        R.id.id_tracks -> openFragment(TracksFragment.newInstance())
        R.id.id_settings -> openFragment(SettingsPreferenceFragment())
      }
      true
    }
  }
}
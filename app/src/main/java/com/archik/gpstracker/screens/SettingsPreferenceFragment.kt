package com.archik.gpstracker.screens

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.archik.gpstracker.R

class SettingsPreferenceFragment: PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.settings_preference, rootKey)
  }
}
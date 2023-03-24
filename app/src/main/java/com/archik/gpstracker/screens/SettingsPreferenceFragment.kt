package com.archik.gpstracker.screens

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.archik.gpstracker.R

class SettingsPreferenceFragment: PreferenceFragmentCompat() {

  private lateinit var timePref: Preference
  private lateinit var colorPref: Preference

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.settings_preference, rootKey)

    init()
  }

  private fun init() {
    timePref = findPreference("update_time_key")!!
    colorPref = findPreference("color_key")!!

    val changeListener = onChangeListener()

    timePref.onPreferenceChangeListener = changeListener
    colorPref.onPreferenceChangeListener = changeListener

    initPrefs()
  }


  private fun onChangeListener(): Preference.OnPreferenceChangeListener {
    return Preference.OnPreferenceChangeListener {
      pref, value ->

        when(pref.key) {
          "update_time_key" -> onTimeChange(value.toString())
          "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
        }

      true
    }
  }

  private fun onTimeChange(value: String) {
    val nameArray = resources.getStringArray(R.array.loc_time_update_name)
    val valueArray = resources.getStringArray(R.array.loc_time_update_value)
    val title = timePref.title.toString().substringBefore(":")

    timePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"
  }


  private fun initPrefs() {
    val pref = timePref.preferenceManager.sharedPreferences

    val nameArray = resources.getStringArray(R.array.loc_time_update_name)
    val valueArray = resources.getStringArray(R.array.loc_time_update_value)
    val title = timePref.title
    val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000"))

    timePref.title = "$title: ${nameArray[pos]}"

    // Для цветав
    val trackColor = pref?.getString("color_key", "#FF0941E8")

    colorPref.icon?.setTint(Color.parseColor(trackColor))
  }
}
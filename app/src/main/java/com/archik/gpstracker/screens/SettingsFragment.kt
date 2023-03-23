package com.archik.gpstracker.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.archik.gpstracker.databinding.FragmentSettingsBinding
import com.archik.gpstracker.databinding.FragmentTrackBinding

class SettingsFragment : Fragment() {

  private lateinit var binding: FragmentSettingsBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSettingsBinding.inflate(inflater, container, false)

    return binding.root
  }

  companion object {
    @JvmStatic
    fun newInstance() = SettingsFragment()
  }
}
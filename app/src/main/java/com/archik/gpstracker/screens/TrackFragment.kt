package com.archik.gpstracker.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.archik.gpstracker.MainApp
import com.archik.gpstracker.MainViewModel
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.FragmentTrackBinding
import com.archik.gpstracker.databinding.FragmentTracksBinding
import com.archik.gpstracker.db.TrackAdapter
import com.archik.gpstracker.db.TrackItem
import com.archik.gpstracker.utils.openFragment

class TrackFragment : Fragment() {
  private lateinit var binding: FragmentTrackBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentTrackBinding.inflate(inflater, container, false)

    return binding.root
  }

  companion object {
    @JvmStatic
    fun newInstance() = TrackFragment()
  }
}
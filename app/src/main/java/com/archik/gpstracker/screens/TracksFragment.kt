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
import com.archik.gpstracker.databinding.FragmentTracksBinding
import com.archik.gpstracker.db.TrackAdapter
import com.archik.gpstracker.db.TrackItem
import com.archik.gpstracker.utils.openFragment

class TracksFragment : Fragment(), TrackAdapter.Listener {

  private lateinit var binding: FragmentTracksBinding
  private lateinit var adapter: TrackAdapter

  private val model: MainViewModel by activityViewModels {
    MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentTracksBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    getTracks()

    initRcView()
  }

  private fun getTracks() {
    model.tracks.observe(viewLifecycleOwner) {
      adapter.submitList(it)

      binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
    }
  }

  private fun initRcView() = with(binding) {
    adapter = TrackAdapter(this@TracksFragment) // передаем только фрагмент, а не binding

    rcView.layoutManager = LinearLayoutManager(requireContext())
    rcView.adapter = adapter
  }

  companion object {
    @JvmStatic
    fun newInstance() = TracksFragment()
  }

  override fun onClick(track: TrackItem, type: TrackAdapter.ClickType) {
    when(type) {
      TrackAdapter.ClickType.DELETE -> model.deleteTrack(track)
      TrackAdapter.ClickType.OPEN -> {
        model.currentTrack.value = track
        openFragment(TrackFragment.newInstance())
      }
    }
//    model.deleteTrack(track)
  }
}
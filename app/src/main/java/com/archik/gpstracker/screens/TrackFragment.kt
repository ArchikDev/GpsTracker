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
import com.archik.gpstracker.databinding.FragmentTrackBinding
import com.archik.gpstracker.db.TrackAdapter
import com.archik.gpstracker.db.TrackItem

class TrackFragment : Fragment(), TrackAdapter.Listener {

  private lateinit var binding: FragmentTrackBinding
  private lateinit var adapter: TrackAdapter

  private val model: MainViewModel by activityViewModels {
    MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentTrackBinding.inflate(inflater, container, false)

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
    adapter = TrackAdapter(this@TrackFragment) // передаем только фрагмент, а не binding

    rcView.layoutManager = LinearLayoutManager(requireContext())
    rcView.adapter = adapter
  }

  companion object {
    @JvmStatic
    fun newInstance() = TrackFragment()
  }

  override fun onClick(track: TrackItem) {
    model.deleteTrack(track)
  }
}
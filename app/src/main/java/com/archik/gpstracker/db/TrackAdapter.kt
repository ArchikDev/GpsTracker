package com.archik.gpstracker.db

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.archik.gpstracker.R
import com.archik.gpstracker.databinding.TrackItemBinding

class TrackAdapter(private val listener: Listener): ListAdapter<TrackItem, TrackAdapter.Holder>(Comparator()) {

  class Holder(view: View, private val listener: Listener): RecyclerView.ViewHolder(view), OnClickListener {

    private val binding = TrackItemBinding.bind(view)
    private var trackTemp: TrackItem? = null

    init {
      binding.cvItemTrack.setOnClickListener(this) // Запустится onClick
      binding.ibDelete.setOnClickListener(this) // Запустится onClick
    }

    fun bind(track: TrackItem) = with(binding) {
      val speed = "${track.speed} km/h"
      val time = "${track.time} s"
      val distance = "${track.distance} km"

      trackTemp = track

      tvData.text = track.date
      tvTime.text = time
      tvDistance.text = distance
      tvSpeed.text = speed
    }

    override fun onClick(v: View?) {
      val type = when(v?.id) {
        R.id.ibDelete -> ClickType.DELETE
        R.id.cvItemTrack -> ClickType.OPEN
        else -> ClickType.OPEN
      }
      trackTemp?.let { listener.onClick(it, type) }
    }
  }

  class Comparator: DiffUtil.ItemCallback<TrackItem>() {
    override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
      return oldItem == newItem
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)

    return Holder(view, listener)
  }

  override fun onBindViewHolder(holder: Holder, position: Int) {
    holder.bind(getItem(position))
  }

  interface Listener {
    fun onClick(track: TrackItem, type: ClickType)
  }

  enum class ClickType {
    DELETE,
    OPEN
  }

}
package com.archik.gpstracker

import androidx.lifecycle.*
import com.archik.gpstracker.db.MainDb
import com.archik.gpstracker.db.TrackItem
import com.archik.gpstracker.location.LocationModel
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MainViewModel(db: MainDb): ViewModel() {
  val dao = db.getDao()
  val timeDate = MutableLiveData<String>()
  val locationUpdates = MutableLiveData<LocationModel>()
  val currentTrack = MutableLiveData<TrackItem>()
  val tracks = dao.getAllTracks().asLiveData()

  fun insertTrack(trackItem: TrackItem) = viewModelScope.launch {
    dao.insertTrack(trackItem)
  }

  fun deleteTrack(trackItem: TrackItem) = viewModelScope.launch {
    dao.deleteTrack(trackItem)
  }

  class ViewModelFactory(private val db: MainDb) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

      if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
        return MainViewModel(db) as T
      }

      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
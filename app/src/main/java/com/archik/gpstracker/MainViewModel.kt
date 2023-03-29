package com.archik.gpstracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.archik.gpstracker.location.LocationModel

class MainViewModel: ViewModel() {
  val timeDate = MutableLiveData<String>()
  val locationUpdates = MutableLiveData<LocationModel>()
}
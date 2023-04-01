package com.archik.gpstracker

import android.app.Application
import com.archik.gpstracker.db.MainDb

class MainApp: Application() {
  val database by lazy { MainDb.getDatabase(this) }
}
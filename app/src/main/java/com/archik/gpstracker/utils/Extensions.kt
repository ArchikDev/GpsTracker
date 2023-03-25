package com.archik.gpstracker.utils

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.fragment.app.Fragment
import com.archik.gpstracker.R

fun Fragment.openFragment(f: Fragment) {
  (activity as AppCompatActivity).supportFragmentManager
    .beginTransaction()
    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    .replace(R.id.containerFrg, f)
    .commit()
}

fun AppCompatActivity.openFragment(f: Fragment) {

  if (supportFragmentManager.fragments.isNotEmpty()) {
    if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
  }

  supportFragmentManager
    .beginTransaction()
    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    .replace(R.id.containerFrg, f)
    .commit()
}

fun Fragment.showToast(s: String) {
  Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.showToast(s: String) {
  Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun Fragment.checkPermission(p: String): Boolean {
  // PackageManager.PERMISSION_GRANTED - есть разрешение
  return when(PackageManager.PERMISSION_GRANTED) {
    ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) -> true
    else -> false
  }
}
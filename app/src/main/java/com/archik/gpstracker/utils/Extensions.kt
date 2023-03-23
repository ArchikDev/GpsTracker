package com.archik.gpstracker.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.archik.gpstracker.R

fun Fragment.openFragment(f: Fragment) {
  (activity as AppCompatActivity).supportFragmentManager
    .beginTransaction()
    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
    .replace(R.id.containerFrg, f)
    .commit()
}

fun AppCompatActivity.openFragment(f: Fragment) {
  supportFragmentManager
    .beginTransaction()
    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
    .replace(R.id.containerFrg, f)
    .commit()
}

fun Fragment.showToast(s: String) {
  Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.showToast(s: String) {
  Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
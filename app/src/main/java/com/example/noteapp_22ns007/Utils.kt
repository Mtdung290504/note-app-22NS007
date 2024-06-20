package com.example.noteapp_22ns007

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class Utils {
    companion object {
        fun notification(view: View?, message: String, actionText: String?, action: (() -> Unit)?) {
            view?.let {
                Snackbar.make(it, message, Snackbar.LENGTH_INDEFINITE)
                    .setDuration(if(actionText.isNullOrBlank()) 1500 else 3000)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(Color.rgb(70, 70, 70))
                    .setTextColor(Color.rgb(240, 240, 240))
                    .setAction(actionText) {
                        if(action != null)
                            action()
                    }
                    .setActionTextColor(Color.WHITE)
                    .show()
            }
        }
    }
}
package com.coolcats.challengehiking.util

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.Snackbar

class CHUtils {

    companion object {
        fun showError(view: View, msg: String) =
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .setTextColor(Color.WHITE)
                .show()

        fun showMsg(view: View, msg: String) =
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.YELLOW)
                .setTextColor(Color.GRAY)
                .show()
    }

}
package com.coolcats.challengehiking.util

import android.util.Log

class Logger {

    companion object{
        private const val TAG_DEBUG = "ME_D"
        private const val TAG_ERROR = "ME_E"

        fun logD(msg: String) = Log.d(TAG_DEBUG, msg)
        fun logE(msg: String) = Log.e(TAG_ERROR, msg)
    }

}
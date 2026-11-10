package com.kit.crashreport.internal

import android.util.Log

internal object CrashLogger {
    private const val TAG = "CrashReportKit"

    fun w(message: String) {
        try {
            Log.w(TAG, message)
        } catch (_: Throwable) {
            println("WARN: [$TAG] $message")
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        } catch (_: Throwable) {
            System.err.println("ERROR: [$TAG] $message")
            throwable?.printStackTrace()
        }
    }
}

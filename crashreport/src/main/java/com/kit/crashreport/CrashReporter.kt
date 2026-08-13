package com.kit.crashreport

/**
 * Backend contract for crash reporting. Host apps normally use [CrashReport];
 * implement this only when plugging in a custom reporter.
 */
interface CrashReporter {
    fun setCollectionEnabled(enabled: Boolean)
    fun setUserId(userId: String?)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
    fun setCustomKey(key: String, value: Long)
    fun setCustomKey(key: String, value: Float)
    fun setCustomKey(key: String, value: Double)
    fun log(message: String)
    fun recordException(throwable: Throwable)
}

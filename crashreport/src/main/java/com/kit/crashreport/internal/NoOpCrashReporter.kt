package com.kit.crashreport.internal

import com.kit.crashreport.CrashReporter

internal object NoOpCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
    override fun setCustomKey(key: String, value: Boolean) = Unit
    override fun setCustomKey(key: String, value: Int) = Unit
    override fun setCustomKey(key: String, value: Long) = Unit
    override fun setCustomKey(key: String, value: Float) = Unit
    override fun setCustomKey(key: String, value: Double) = Unit
    override fun log(message: String) = Unit
    override fun recordException(throwable: Throwable) = Unit
}

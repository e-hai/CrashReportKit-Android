package com.kit.crashreport.internal

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kit.crashreport.CrashReporter

/**
 * Temporary Firebase Crashlytics adapter. Replace by swapping [CrashBackend]
 * when another vendor is ready.
 */
internal class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
) : CrashReporter {

    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun setUserId(userId: String?) {
        crashlytics.setUserId(userId.orEmpty())
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}

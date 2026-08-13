package com.kit.crashreport

import android.content.Context
import com.kit.crashreport.internal.FirebaseCrashReporter
import com.kit.crashreport.internal.NoOpCrashReporter

/**
 * Crash reporting SDK facade.
 *
 * Call [init] once in `Application.onCreate`. Business code should only depend
 * on this object so the underlying vendor (currently Firebase Crashlytics)
 * can be swapped later without touching call sites.
 *
 * Host app requirements when using [CrashBackend.FIREBASE]:
 * - Apply `com.google.gms.google-services` and `com.google.firebase.crashlytics`
 * - Place `google-services.json` in the application module
 */
object CrashReport {

    @Volatile
    private var reporter: CrashReporter = NoOpCrashReporter

    @Volatile
    private var initialized: Boolean = false

    @JvmStatic
    @JvmOverloads
    fun init(context: Context, config: CrashReportConfig = CrashReportConfig()) {
        if (initialized) return

        reporter = if (!config.enabled) {
            NoOpCrashReporter
        } else {
            when (config.backend) {
                CrashBackend.FIREBASE -> FirebaseCrashReporter()
            }
        }

        // Touch application context so callers can pass Activity safely.
        context.applicationContext
        reporter.setCollectionEnabled(config.collectionEnabled)
        initialized = true
    }

    /**
     * Inject a custom [CrashReporter]. Prefer [init] for normal use;
     * this is mainly for tests or future backend adapters.
     */
    @JvmStatic
    fun setReporter(customReporter: CrashReporter) {
        reporter = customReporter
        initialized = true
    }

    @JvmStatic
    fun isInitialized(): Boolean = initialized

    @JvmStatic
    fun setCollectionEnabled(enabled: Boolean) {
        reporter.setCollectionEnabled(enabled)
    }

    @JvmStatic
    fun setUserId(userId: String?) {
        reporter.setUserId(userId)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Int) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Long) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Float) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Double) {
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun log(message: String) {
        reporter.log(message)
    }

    @JvmStatic
    fun recordException(throwable: Throwable) {
        reporter.recordException(throwable)
    }

    /** Forces a fatal crash. Use only in debug / QA builds. */
    @JvmStatic
    fun testCrash() {
        throw RuntimeException("CrashReport test crash")
    }
}

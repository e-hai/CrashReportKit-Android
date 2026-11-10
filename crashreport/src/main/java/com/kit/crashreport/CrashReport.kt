package com.kit.crashreport

import android.content.Context
import com.kit.crashreport.internal.CrashLogger
import com.kit.crashreport.internal.FirebaseCrashReporter
import com.kit.crashreport.internal.NoOpCrashReporter
import java.util.concurrent.atomic.AtomicBoolean

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

    @Volatile
    private var status: CrashReportStatus = CrashReportStatus.NOT_INITIALIZED

    @Volatile
    private var lastInitResult: CrashReportInitResult? = null

    private val hasWarnedUninitialized = AtomicBoolean(false)

    /**
     * Initializes the CrashReport SDK.
     *
     * Overloaded for full backward compatibility:
     * - `CrashReport.init(context)`
     * - `CrashReport.init(context, config)`
     * - `CrashReport.init(context, config, callback)` / Kotlin trailing lambda
     *
     * @param context Application or Activity context.
     * @param config Configuration options. Defaults to [CrashReportConfig].
     * @param callback Optional callback invoked with structured [CrashReportInitResult].
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        config: CrashReportConfig = CrashReportConfig(),
        callback: CrashReportInitCallback? = null,
    ) {
        if (initialized) {
            lastInitResult?.let { callback?.onResult(it) }
            return
        }

        synchronized(this) {
            if (initialized) {
                lastInitResult?.let { callback?.onResult(it) }
                return
            }

            val result = if (!config.enabled) {
                reporter = NoOpCrashReporter
                status = CrashReportStatus.DISABLED
                CrashReportInitResult(
                    status = CrashReportStatus.DISABLED,
                    message = "Crash reporting disabled by CrashReportConfig(enabled = false)."
                )
            } else {
                try {
                    reporter = when (config.backend) {
                        CrashBackend.FIREBASE -> FirebaseCrashReporter()
                    }
                    status = CrashReportStatus.READY
                    CrashReportInitResult(
                        status = CrashReportStatus.READY,
                        message = "Crash reporting initialized successfully with backend: ${config.backend}."
                    )
                } catch (e: Throwable) {
                    reporter = NoOpCrashReporter
                    status = CrashReportStatus.FAILED
                    CrashLogger.e(
                        "Failed to initialize ${config.backend} backend. " +
                                "Safely degraded to NoOpCrashReporter to prevent app crash: ${e.message}",
                        e
                    )
                    CrashReportInitResult(
                        status = CrashReportStatus.FAILED,
                        message = "Failed to initialize ${config.backend} backend: ${e.message}",
                        cause = e
                    )
                }
            }

            // Touch application context so callers can pass Activity safely.
            try {
                context.applicationContext
            } catch (_: Throwable) {
                // Ignore context errors in mock environments
            }

            try {
                reporter.setCollectionEnabled(config.collectionEnabled)
            } catch (e: Throwable) {
                CrashLogger.w("Failed to apply setCollectionEnabled(${config.collectionEnabled}): ${e.message}")
            }

            lastInitResult = result
            initialized = true
            callback?.onResult(result)
        }
    }

    /**
     * Inject a custom [CrashReporter]. Prefer [init] for normal use;
     * this is mainly for tests or future backend adapters.
     */
    @JvmStatic
    fun setReporter(customReporter: CrashReporter) {
        synchronized(this) {
            reporter = customReporter
            status = CrashReportStatus.READY
            lastInitResult = CrashReportInitResult(
                status = CrashReportStatus.READY,
                message = "Custom CrashReporter injected."
            )
            initialized = true
        }
    }

    /**
     * Returns true if [init] or [setReporter] has been called.
     */
    @JvmStatic
    fun isInitialized(): Boolean = initialized

    /**
     * Returns the current operational status of the SDK.
     */
    @JvmStatic
    fun getStatus(): CrashReportStatus = status

    /**
     * Resets internal state for unit test isolation.
     */
    internal fun resetForTesting() {
        synchronized(this) {
            reporter = NoOpCrashReporter
            initialized = false
            status = CrashReportStatus.NOT_INITIALIZED
            lastInitResult = null
            hasWarnedUninitialized.set(false)
        }
    }

    private fun checkInitialized() {
        if (!initialized && hasWarnedUninitialized.compareAndSet(false, true)) {
            CrashLogger.w(
                "CrashReport has not been initialized. Calls to CrashReport.* are safe no-ops. " +
                        "Call CrashReport.init(context) in Application.onCreate."
            )
        }
    }

    @JvmStatic
    fun setCollectionEnabled(enabled: Boolean) {
        checkInitialized()
        reporter.setCollectionEnabled(enabled)
    }

    @JvmStatic
    fun setUserId(userId: String?) {
        checkInitialized()
        reporter.setUserId(userId)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Int) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Long) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Float) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Double) {
        checkInitialized()
        reporter.setCustomKey(key, value)
    }

    @JvmStatic
    fun log(message: String) {
        checkInitialized()
        reporter.log(message)
    }

    @JvmStatic
    fun recordException(throwable: Throwable) {
        checkInitialized()
        reporter.recordException(throwable)
    }

    /** Forces a fatal crash. Use only in debug / QA builds. */
    @JvmStatic
    fun testCrash() {
        throw RuntimeException("CrashReport test crash")
    }
}

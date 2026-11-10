package com.kit.crashreport

/**
 * Current operational status of [CrashReport].
 */
enum class CrashReportStatus {
    /** [CrashReport.init] has not been called yet. */
    NOT_INITIALIZED,

    /** Initialized successfully and backend reporter is active. */
    READY,

    /** Initialized with `enabled = false`; all calls are safe no-ops. */
    DISABLED,

    /**
     * Backend initialization failed (e.g. missing google-services.json or FirebaseApp not configured).
     * The SDK has safely degraded to [NoOpCrashReporter] so the host app will not crash.
     */
    FAILED,
}

/**
 * Structured result delivered via [CrashReportInitCallback] upon initialization completion.
 */
data class CrashReportInitResult(
    val status: CrashReportStatus,
    val message: String? = null,
    val cause: Throwable? = null,
) {
    /** True only when [status] is [CrashReportStatus.READY]. */
    val isReady: Boolean
        get() = status == CrashReportStatus.READY
}

/**
 * Functional callback interface for [CrashReport.init].
 * Supports Kotlin trailing lambda and Java 8+ SAM conversion.
 */
fun interface CrashReportInitCallback {
    fun onResult(result: CrashReportInitResult)
}

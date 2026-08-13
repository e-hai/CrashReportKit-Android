package com.kit.crashreport

/**
 * Configuration for [CrashReport.init].
 *
 * @param enabled Master switch. When false, all calls become no-ops.
 * @param collectionEnabled Whether the backend may upload crash data.
 * @param backend Reporting implementation. Defaults to Firebase Crashlytics.
 */
data class CrashReportConfig(
    val enabled: Boolean = true,
    val collectionEnabled: Boolean = true,
    val backend: CrashBackend = CrashBackend.FIREBASE,
)

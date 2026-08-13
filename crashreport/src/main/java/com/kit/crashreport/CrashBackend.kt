package com.kit.crashreport

/**
 * Crash reporting backend. Swap implementations without changing call sites.
 * Currently only [FIREBASE] is wired; other backends can be added later.
 */
enum class CrashBackend {
    FIREBASE,
}

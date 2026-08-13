package com.kit.crashreport

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrashReportTest {

    @Before
    fun setUp() {
        CrashReport.setReporter(object : CrashReporter {
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
        })
    }

    @Test
    fun isInitialized_afterSetReporter() {
        assertTrue(CrashReport.isInitialized())
    }

    @Test
    fun recordException_doesNotThrow() {
        CrashReport.recordException(RuntimeException("unit-test"))
    }

    @Test
    fun log_doesNotThrow() {
        CrashReport.log("breadcrumb")
    }
}

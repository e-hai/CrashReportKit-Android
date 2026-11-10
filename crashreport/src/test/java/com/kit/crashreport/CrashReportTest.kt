package com.kit.crashreport

import android.content.Context
import android.content.ContextWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrashReportTest {

    private class TestContext : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    @Before
    fun setUp() {
        CrashReport.resetForTesting()
    }

    @Test
    fun uninitialized_stateAndOperationsDoNotThrow() {
        assertFalse(CrashReport.isInitialized())
        assertEquals(CrashReportStatus.NOT_INITIALIZED, CrashReport.getStatus())

        // Calling business methods before init must be safe no-ops and not crash
        CrashReport.log("pre-init breadcrumb")
        CrashReport.recordException(RuntimeException("pre-init exception"))
        CrashReport.setUserId("pre-init-user")
        CrashReport.setCustomKey("key", "val")
        CrashReport.setCollectionEnabled(false)
    }

    @Test
    fun setReporter_updatesStatusToReady() {
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

        assertTrue(CrashReport.isInitialized())
        assertEquals(CrashReportStatus.READY, CrashReport.getStatus())
        CrashReport.recordException(RuntimeException("unit-test"))
        CrashReport.log("breadcrumb")
    }

    @Test
    fun initWithDisabledConfig_invokesCallbackWithDisabledStatus() {
        var callbackResult: CrashReportInitResult? = null

        CrashReport.init(
            context = TestContext(),
            config = CrashReportConfig(enabled = false),
        ) { result ->
            callbackResult = result
        }

        assertTrue(CrashReport.isInitialized())
        assertEquals(CrashReportStatus.DISABLED, CrashReport.getStatus())
        assertNotNull(callbackResult)
        assertEquals(CrashReportStatus.DISABLED, callbackResult?.status)
        assertFalse(callbackResult?.isReady ?: true)

        // Repeat init call dispatches cached result
        var secondCallbackResult: CrashReportInitResult? = null
        CrashReport.init(TestContext()) { result ->
            secondCallbackResult = result
        }
        assertEquals(CrashReportStatus.DISABLED, secondCallbackResult?.status)
    }

    @Test
    fun initWithMissingFirebase_catchesExceptionAndDegradesToFailedWithoutCrashing() {
        var callbackResult: CrashReportInitResult? = null

        // In a plain local unit test, FirebaseApp is not initialized.
        // The SDK must catch the IllegalStateException, degrade to NoOp, and report FAILED.
        CrashReport.init(
            context = TestContext(),
            config = CrashReportConfig(enabled = true, backend = CrashBackend.FIREBASE),
        ) { result ->
            callbackResult = result
        }

        assertTrue(CrashReport.isInitialized())
        assertEquals(CrashReportStatus.FAILED, CrashReport.getStatus())
        assertNotNull(callbackResult)
        assertEquals(CrashReportStatus.FAILED, callbackResult?.status)
        assertFalse(callbackResult?.isReady ?: true)
        assertNotNull(callbackResult?.cause)

        // After failure, operations must still be safe NoOps
        CrashReport.log("post-fail log")
        CrashReport.recordException(IllegalStateException("post-fail exception"))
    }

    @Test
    fun backwardCompatibility_initWithoutCallbackWorks() {
        // Calling init with 1 argument or 2 arguments without callback must compile and run
        CrashReport.init(TestContext())
        assertTrue(CrashReport.isInitialized())
    }
}

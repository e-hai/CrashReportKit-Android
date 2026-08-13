package com.kit.crashreport.sample

import android.app.Application
import com.kit.crashreport.CrashReport
import com.kit.crashreport.CrashReportConfig

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReport.init(
            context = this,
            config = CrashReportConfig(
                enabled = true,
                collectionEnabled = true,
            ),
        )
        CrashReport.setUserId("sample-user")
        CrashReport.setCustomKey("app_flavor", "sample")
    }
}

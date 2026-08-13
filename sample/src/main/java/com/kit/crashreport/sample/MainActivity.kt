package com.kit.crashreport.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kit.crashreport.CrashReport
import com.kit.crashreport.sample.ui.theme.CrashReportKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrashReportKitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrashDemoScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLog = {
                            CrashReport.log("Sample breadcrumb from MainActivity")
                            Toast.makeText(this, "Logged breadcrumb", Toast.LENGTH_SHORT).show()
                        },
                        onNonFatal = {
                            CrashReport.recordException(
                                IllegalStateException("Sample non-fatal exception"),
                            )
                            Toast.makeText(this, "Recorded non-fatal", Toast.LENGTH_SHORT).show()
                        },
                        onFatal = {
                            CrashReport.testCrash()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun CrashDemoScreen(
    modifier: Modifier = Modifier,
    onLog: () -> Unit = {},
    onNonFatal: () -> Unit = {},
    onFatal: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "CrashReportKit",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Backend: Firebase Crashlytics (temporary)",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onLog,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log breadcrumb")
        }
        Button(
            onClick = onNonFatal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Record non-fatal")
        }
        Button(
            onClick = onFatal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Force fatal crash")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrashDemoPreview() {
    CrashReportKitTheme {
        CrashDemoScreen()
    }
}

package com.abeyytechxy.cnguard

import android.app.Activity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeController
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeSnapshot
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeState
import com.abeyytechxy.cnguard.diagnostics.BaselineDiagnostics
import com.abeyytechxy.cnguard.diagnostics.DiagnosticState

class MainActivity : Activity() {
    private lateinit var localBridgeController: LocalBridgeController
    private lateinit var localBridgeStatus: TextView
    private lateinit var scanBridgeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localBridgeController = LocalBridgeController(this)

        val density = resources.displayMetrics.density
        val padding = (20 * density).toInt()
        val spacing = (12 * density).toInt()

        val output = TextView(this).apply {
            textSize = 16f
            setTextIsSelectable(true)
            movementMethod = ScrollingMovementMethod.getInstance()
            text = renderReport()
        }

        localBridgeStatus = TextView(this).apply {
            textSize = 16f
            text = "Local Bridge: not scanned"
        }

        scanBridgeButton = Button(this).apply {
            text = "Scan Local Bridge"
            setOnClickListener {
                isEnabled = false
                localBridgeStatus.text = "Local Bridge: scanning Wireless ADB services..."
                localBridgeController.discover { snapshot ->
                    runOnUiThread {
                        localBridgeStatus.text = renderBridgeSnapshot(snapshot)
                        isEnabled = true
                    }
                }
            }
        }

        val developerOptionsButton = Button(this).apply {
            text = "Open Developer Options"
            setOnClickListener {
                localBridgeController.openDeveloperOptions()
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)

            addView(
                output,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            addSpacer(spacing)
            addView(localBridgeStatus)
            addSpacer(spacing)
            addView(scanBridgeButton)
            addView(developerOptionsButton)
        }

        setContentView(
            ScrollView(this).apply {
                addView(
                    content,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        )
    }

    override fun onDestroy() {
        localBridgeController.close()
        super.onDestroy()
    }

    private fun LinearLayout.addSpacer(height: Int) {
        addView(
            TextView(this@MainActivity),
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        )
    }

    private fun renderBridgeSnapshot(snapshot: LocalBridgeSnapshot): String {
        val summary = when (snapshot.state) {
            LocalBridgeState.IDLE -> "idle"
            LocalBridgeState.DISCOVERING -> "scanning"
            LocalBridgeState.AVAILABLE -> "Wireless ADB available"
            LocalBridgeState.PAIRING_REQUIRED -> "pairing mode detected"
            LocalBridgeState.UNAVAILABLE -> "Wireless ADB not detected"
            LocalBridgeState.ERROR -> "discovery error"
        }

        return buildString {
            append("Local Bridge: ")
            append(summary)

            snapshot.connectService?.let {
                appendLine()
                append("Connect service: ")
                append(it.serviceName)
            }

            snapshot.pairingService?.let {
                appendLine()
                append("Pairing service: ")
                append(it.serviceName)
            }

            snapshot.detail?.let {
                appendLine()
                append(it)
            }
        }
    }

    private fun renderReport(): String {
        val report = BaselineDiagnostics.collect(this)

        return buildString {
            appendLine("986 CN Guard")
            appendLine("Read-only diagnostic baseline")
            appendLine()
            appendLine(report.deviceSummary)
            appendLine()

            report.items.forEach { item ->
                val marker = when (item.state) {
                    DiagnosticState.PASS -> "[PASS]"
                    DiagnosticState.ATTENTION -> "[CHECK]"
                    DiagnosticState.INFO -> "[INFO]"
                    DiagnosticState.UNKNOWN -> "[UNKNOWN]"
                    DiagnosticState.UNSUPPORTED -> "[UNSUPPORTED]"
                }

                appendLine("$marker ${item.title}")
                appendLine("  ${item.summary}")
                item.detail?.let { appendLine("  $it") }
                appendLine()
            }
        }
    }
}

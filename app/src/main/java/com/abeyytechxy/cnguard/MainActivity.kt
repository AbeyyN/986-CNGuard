package com.abeyytechxy.cnguard

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeController
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeDiagnostic
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeResult
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeSnapshot
import com.abeyytechxy.cnguard.bridge.localadb.LocalBridgeState
import com.abeyytechxy.cnguard.diagnostics.BaselineDiagnostics
import com.abeyytechxy.cnguard.diagnostics.DiagnosticState

class MainActivity : Activity() {
    private lateinit var localBridgeController: LocalBridgeController
    private lateinit var localBridgeStatus: TextView
    private lateinit var scanBridgeButton: Button
    private lateinit var pairButton: Button
    private lateinit var testBridgeButton: Button
    private lateinit var diagnosticsButton: Button
    private lateinit var pairingCodeInput: EditText

    private var bridgeSnapshot = LocalBridgeSnapshot(LocalBridgeState.IDLE)

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
            setTextIsSelectable(true)
            text = "Local Bridge: not scanned"
        }

        pairingCodeInput = EditText(this).apply {
            hint = "6-digit pairing code"
            inputType = InputType.TYPE_CLASS_NUMBER
            isSingleLine = true
        }

        scanBridgeButton = Button(this).apply {
            text = "Scan Local Bridge"
            setOnClickListener { scanBridge() }
        }

        pairButton = Button(this).apply {
            text = "Pair Local Bridge"
            isEnabled = false
            setOnClickListener {
                setBridgeBusy(true, "Pairing with local Wireless ADB...")
                localBridgeController.pair(
                    service = bridgeSnapshot.pairingService,
                    pairingCode = pairingCodeInput.text.toString()
                ) { result ->
                    runOnUiThread {
                        setBridgeBusy(false, renderResult(result))
                        scanBridge()
                    }
                }
            }
        }

        testBridgeButton = Button(this).apply {
            text = "Test Bridge"
            isEnabled = false
            setOnClickListener {
                setBridgeBusy(true, "Testing authenticated ADB connection...")
                localBridgeController.testConnection(bridgeSnapshot.connectService) { result ->
                    runOnUiThread {
                        setBridgeBusy(false, renderResult(result))
                    }
                }
            }
        }

        diagnosticsButton = Button(this).apply {
            text = "Run Bridge Diagnostics"
            isEnabled = false
            setOnClickListener {
                setBridgeBusy(true, "Running read-only bridge diagnostics...")
                localBridgeController.runReadOnlyDiagnostics(bridgeSnapshot.connectService) { diagnostics ->
                    runOnUiThread {
                        setBridgeBusy(false, renderDiagnostics(diagnostics))
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

            addView(output)
            addSpacer(spacing)
            addView(localBridgeStatus)
            addSpacer(spacing)
            addView(scanBridgeButton)
            addView(pairingCodeInput)
            addView(pairButton)
            addView(testBridgeButton)
            addView(diagnosticsButton)
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

    private fun scanBridge() {
        setBridgeBusy(true, "Local Bridge: scanning local Wireless ADB services...")
        localBridgeController.discover { snapshot ->
            runOnUiThread {
                bridgeSnapshot = snapshot
                setBridgeBusy(false, renderBridgeSnapshot(snapshot))
                updateBridgeActions()
            }
        }
    }

    private fun updateBridgeActions() {
        pairButton.isEnabled = bridgeSnapshot.pairingService?.isResolved == true
        testBridgeButton.isEnabled = bridgeSnapshot.connectService?.isResolved == true
        diagnosticsButton.isEnabled = bridgeSnapshot.connectService?.isResolved == true
    }

    private fun setBridgeBusy(busy: Boolean, status: String) {
        localBridgeStatus.text = status
        scanBridgeButton.isEnabled = !busy
        pairingCodeInput.isEnabled = !busy
        if (busy) {
            pairButton.isEnabled = false
            testBridgeButton.isEnabled = false
            diagnosticsButton.isEnabled = false
        } else {
            updateBridgeActions()
        }
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
                append("Connect: ")
                append(it.host ?: "unresolved")
                append(':')
                append(it.port ?: 0)
            }

            snapshot.pairingService?.let {
                appendLine()
                append("Pairing: ")
                append(it.host ?: "unresolved")
                append(':')
                append(it.port ?: 0)
            }

            snapshot.detail?.let {
                appendLine()
                append(it)
            }
        }
    }

    private fun renderResult(result: LocalBridgeResult): String {
        return buildString {
            append(if (result.success) "Local Bridge: PASS" else "Local Bridge: CHECK")
            appendLine()
            append(result.summary)
            result.detail?.takeIf(String::isNotBlank)?.let {
                appendLine()
                append(it)
            }
        }
    }

    private fun renderDiagnostics(diagnostics: List<LocalBridgeDiagnostic>): String {
        return buildString {
            appendLine("Local Bridge diagnostics")
            diagnostics.forEach { item ->
                append(if (item.available) "[AVAILABLE] " else "[UNKNOWN] ")
                appendLine(item.summary)
                item.detail?.takeIf(String::isNotBlank)?.let {
                    appendLine("  $it")
                }
            }
        }.trim()
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

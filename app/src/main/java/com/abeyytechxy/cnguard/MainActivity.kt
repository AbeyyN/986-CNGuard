package com.abeyytechxy.cnguard

import android.app.Activity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import com.abeyytechxy.cnguard.diagnostics.BaselineDiagnostics
import com.abeyytechxy.cnguard.diagnostics.DiagnosticState

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val padding = (20 * density).toInt()

        val output = TextView(this).apply {
            setPadding(padding, padding, padding, padding)
            textSize = 16f
            setTextIsSelectable(true)
            movementMethod = ScrollingMovementMethod.getInstance()
            text = renderReport()
        }

        setContentView(
            ScrollView(this).apply {
                addView(
                    output,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        )
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

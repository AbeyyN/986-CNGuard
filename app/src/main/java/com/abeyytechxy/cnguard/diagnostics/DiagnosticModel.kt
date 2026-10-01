package com.abeyytechxy.cnguard.diagnostics

enum class DiagnosticState {
    PASS,
    ATTENTION,
    INFO,
    UNKNOWN,
    UNSUPPORTED
}

data class DiagnosticItem(
    val id: String,
    val state: DiagnosticState,
    val title: String,
    val summary: String,
    val detail: String? = null
)

data class DiagnosticReport(
    val deviceSummary: String,
    val items: List<DiagnosticItem>
)

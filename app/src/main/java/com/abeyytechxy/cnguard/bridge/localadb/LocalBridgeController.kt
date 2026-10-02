package com.abeyytechxy.cnguard.bridge.localadb

import android.app.Activity
import android.content.Intent
import android.provider.Settings

class LocalBridgeController(
    private val activity: Activity
) {
    private val discovery = LocalAdbDiscovery(activity)
    private val transport = LocalBridgeTransport(activity)

    fun discover(callback: (LocalBridgeSnapshot) -> Unit) {
        discovery.discover(callback = callback)
    }

    fun pair(
        service: LocalAdbService?,
        pairingCode: String,
        callback: (LocalBridgeResult) -> Unit
    ) {
        transport.pair(service, pairingCode, callback)
    }

    fun testConnection(
        service: LocalAdbService?,
        callback: (LocalBridgeResult) -> Unit
    ) {
        transport.testConnection(service, callback)
    }

    fun runReadOnlyDiagnostics(
        service: LocalAdbService?,
        callback: (List<LocalBridgeDiagnostic>) -> Unit
    ) {
        transport.runReadOnlyDiagnostics(service, callback)
    }

    fun openDeveloperOptions() {
        val preferred = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        val fallback = Intent(Settings.ACTION_SETTINGS)

        val intent = if (preferred.resolveActivity(activity.packageManager) != null) {
            preferred
        } else {
            fallback
        }

        activity.startActivity(intent)
    }

    fun close() {
        discovery.close()
        transport.close()
    }
}

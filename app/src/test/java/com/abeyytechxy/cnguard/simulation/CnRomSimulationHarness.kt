package com.abeyytechxy.cnguard.simulation

import com.abeyytechxy.cnguard.bridge.localadb.FcmSocketParser
import com.abeyytechxy.cnguard.bridge.localadb.LocalAdbService
import com.abeyytechxy.cnguard.bridge.localadb.PairingCodeValidator
import com.abeyytechxy.cnguard.diagnostics.DeviceFamilyClassifier
import com.abeyytechxy.cnguard.diagnostics.DiagnosticState
import com.abeyytechxy.cnguard.diagnostics.FcmNetworkPolicy
import com.abeyytechxy.cnguard.diagnostics.FcmNetworkState
import com.abeyytechxy.cnguard.diagnostics.XiaomiFamilyState
import com.abeyytechxy.cnguard.notification.NotificationPresentationClassifier
import com.abeyytechxy.cnguard.notification.PresentationState

enum class SimulatedSocketState {
    PRESENT,
    ABSENT,
    UNKNOWN
}

data class CnRomSimulationFixture(
    val name: String,
    val manufacturer: String?,
    val brand: String?,
    val securityCenterPresent: Boolean?,
    val powerKeeperPresent: Boolean?,
    val dnsResolved: Boolean,
    val reachablePorts: Set<Int>,
    val gmsUid: Int,
    val procNetTcp: String?,
    val notificationImportance: Int,
    val packageSuspended: Boolean,
    val suppressedVisualEffects: Int,
    val pairingService: LocalAdbService?,
    val connectService: LocalAdbService?,
    val pairingCode: String
)

data class CnRomSimulationResult(
    val family: XiaomiFamilyState,
    val romRegion: DiagnosticState,
    val network: FcmNetworkState,
    val fcmSocket: SimulatedSocketState,
    val notificationPresentation: PresentationState,
    val pairingEndpointResolved: Boolean,
    val connectEndpointResolved: Boolean,
    val pairingCodeAccepted: Boolean
)

object CnRomSimulationHarness {
    fun evaluate(fixture: CnRomSimulationFixture): CnRomSimulationResult {
        val family = DeviceFamilyClassifier.classify(
            manufacturer = fixture.manufacturer,
            brand = fixture.brand,
            securityCenterPresent = fixture.securityCenterPresent,
            powerKeeperPresent = fixture.powerKeeperPresent
        )

        val network = FcmNetworkPolicy.evaluate(
            dnsResolved = fixture.dnsResolved,
            reachablePorts = fixture.reachablePorts
        )

        val fcmSocket = fixture.procNetTcp?.let { raw ->
            if (FcmSocketParser.hasEstablishedSocket(raw, fixture.gmsUid)) {
                SimulatedSocketState.PRESENT
            } else {
                SimulatedSocketState.ABSENT
            }
        } ?: SimulatedSocketState.UNKNOWN

        val presentation = NotificationPresentationClassifier.classify(
            importance = fixture.notificationImportance,
            suspended = fixture.packageSuspended,
            suppressedVisualEffects = fixture.suppressedVisualEffects
        )

        return CnRomSimulationResult(
            family = family,
            // Synthetic fixtures exercise CN-like behavior, but never prove ROM region.
            romRegion = DiagnosticState.UNKNOWN,
            network = network.state,
            fcmSocket = fcmSocket,
            notificationPresentation = presentation.state,
            pairingEndpointResolved = fixture.pairingService?.isResolved == true,
            connectEndpointResolved = fixture.connectService?.isResolved == true,
            pairingCodeAccepted = PairingCodeValidator.isValid(fixture.pairingCode)
        )
    }
}

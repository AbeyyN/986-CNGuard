package com.abeyytechxy.cnguard.diagnostics

import android.os.SystemClock
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

enum class FcmNetworkState {
    TCP_ENDPOINT_REACHABLE,
    NO_TCP_CONNECTION_OBSERVED,
    DNS_UNAVAILABLE
}

data class FcmNetworkResult(
    val state: FcmNetworkState,
    val reachablePorts: List<Int>,
    val checkedPorts: List<Int>,
    val summary: String
)

object FcmNetworkPolicy {
    val ports: List<Int> = listOf(5228, 5229, 5230, 443)

    fun evaluate(dnsResolved: Boolean, reachablePorts: Collection<Int>): FcmNetworkResult {
        val confirmed = ports.filter { it in reachablePorts }
        val state = when {
            !dnsResolved -> FcmNetworkState.DNS_UNAVAILABLE
            confirmed.isNotEmpty() -> FcmNetworkState.TCP_ENDPOINT_REACHABLE
            else -> FcmNetworkState.NO_TCP_CONNECTION_OBSERVED
        }

        val summary = when (state) {
            FcmNetworkState.DNS_UNAVAILABLE ->
                "Google endpoint DNS resolution unavailable on the current network."
            FcmNetworkState.TCP_ENDPOINT_REACHABLE ->
                "Google endpoint TCP connection observed on ports " +
                    confirmed.joinToString(", ") +
                    ". This does not prove FCM registration or message delivery."
            FcmNetworkState.NO_TCP_CONNECTION_OBSERVED ->
                "No TCP connection to the tested Google endpoint/ports was observed. " +
                    "This does not prove FCM is blocked on every Google address."
        }

        return FcmNetworkResult(state, confirmed, ports, summary)
    }
}

/**
 * Opt-in, TCP connect-only network probe. Never sends a token, notification,
 * application data, or HTTP request.
 */
object FcmNetworkProbe {
    private const val HOST = "mtalk.google.com"
    private const val CONNECT_TIMEOUT_MS = 1_500

    fun run(): FcmNetworkResult {
        val addresses = runCatching {
            InetAddress.getAllByName(HOST)
                .filter { !it.isAnyLocalAddress && !it.isLoopbackAddress }
                .take(2)
        }.getOrDefault(emptyList())

        if (addresses.isEmpty()) {
            return FcmNetworkPolicy.evaluate(dnsResolved = false, reachablePorts = emptyList())
        }

        val reachable = mutableSetOf<Int>()
        for (port in FcmNetworkPolicy.ports) {
            if (Thread.currentThread().isInterrupted) break
            for (address in addresses) {
                if (Thread.currentThread().isInterrupted) break
                val connected = runCatching {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(address, port), CONNECT_TIMEOUT_MS)
                        socket.isConnected
                    }
                }.getOrDefault(false)

                if (connected) {
                    reachable.add(port)
                    break
                }
            }
        }

        return FcmNetworkPolicy.evaluate(dnsResolved = true, reachablePorts = reachable)
    }
}

package com.subreax.reaction.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkConnectionObserver(appContext: Context) : ConnectionObserver {
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun status(): Flow<ConnectionObserver.Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isNetAvailable = true
                notify(ConnectionObserver.Status.Connected)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                isNetAvailable = false
                notify(ConnectionObserver.Status.Disconnected)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isNetAvailable = false
                notify(ConnectionObserver.Status.Disconnected)
            }

            private fun notify(status: ConnectionObserver.Status) {
                launch {
                    send(status)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.requestNetwork(networkRequest, callback)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    var isNetAvailable: Boolean = false
        private set
}
package com.subreax.reaction.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class ApplicationState {
    WaitingForNetwork, Connecting, Syncing, Ready
}

class ApplicationStateSource(applicationContext: Context) {
    val state = MutableStateFlow(ApplicationState.WaitingForNetwork)

    private val connObserver = NetworkConnectionObserver(applicationContext)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val connectingActions = mutableListOf<suspend () -> Boolean>()
    private val syncingActions = mutableListOf<suspend () -> Boolean>()
    private var onErrorAction: suspend (Boolean, ApplicationState) -> ApplicationState =
        this::defaultOnErrorAction


    fun start() {
        coroutineScope.launch {
            state.collect {
                Log.d(TAG, "=== Application state changed: $it")
                when (it) {
                    ApplicationState.Connecting -> {
                        if (doConnectingActions()) {
                            state.value = ApplicationState.Syncing
                        }
                        else {
                            handleError()
                        }
                    }
                    ApplicationState.Syncing -> {
                        if (doSyncingActions()) {
                            state.value = ApplicationState.Ready
                        }
                        else {
                            handleError()
                        }
                    }
                    /*ApplicationState.Ready -> {
                        doOnReadyActions()
                    }*/
                    else -> {}
                }
            }
        }

        coroutineScope.launch {
            connObserver.status().collect { status ->
                if (status == ConnectionObserver.Status.Connected) {
                    state.value = ApplicationState.Connecting
                }
                else if (status == ConnectionObserver.Status.Disconnected) {
                    state.value = ApplicationState.WaitingForNetwork
                }
            }
        }
    }

    fun restart() {
        if (connObserver.isNetAvailable) {
            state.value = ApplicationState.Connecting
        }
        else {
            state.value = ApplicationState.WaitingForNetwork
        }
    }

    private suspend fun defaultOnErrorAction(
        isNetworkAvailable: Boolean,
        appState: ApplicationState
    ): ApplicationState {
        delay(5000)
        return  if (isNetworkAvailable)
                    ApplicationState.Connecting
                else
                    ApplicationState.WaitingForNetwork
    }

    fun addConnectingAction(action: suspend () -> Boolean) {
        connectingActions.add(action)
    }

    fun removeConnectingAction(action: suspend () -> Boolean) {
        connectingActions.remove(action)
    }

    fun addSyncingAction(action: suspend () -> Boolean) {
        syncingActions.add(action)
    }

    fun removeSyncingAction(action: suspend () -> Boolean) {
        syncingActions.remove(action)
    }

    private suspend fun doConnectingActions(): Boolean {
        for (action in connectingActions) {
            if (!action()) {
                return false
            }
        }
        return true
    }

    private suspend fun doSyncingActions(): Boolean {
        for (action in syncingActions) {
            if (!action()) {
                return false
            }
        }
        return true
    }

    private suspend fun handleError() {
        state.value = onErrorAction(connObserver.isNetAvailable, state.value)
    }

    companion object {
        private const val TAG = "ApplicationStateSource"
    }
}

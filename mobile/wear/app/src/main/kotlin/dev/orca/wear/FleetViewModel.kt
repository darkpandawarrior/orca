package dev.orca.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay

class FleetViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SnapshotStore(app)

    // Ticks so a snapshot that stops updating flips to stale without a new event.
    private val clock = flow { while (true) { emit(System.currentTimeMillis()); delay(30_000L) } }

    val ui: StateFlow<FleetUi?> =
        combine(store.snapshot, PhoneLink.reachable, clock) { s, reachable, now ->
            FleetPresentation.toUi(s, reachable, now)
        }.map<FleetUi, FleetUi?> { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

package dev.orca.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class WearActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugSeeder.seedIfEmpty(applicationContext)
        // Reachability needs Play services; on an AVD without them the poll just reports false.
        lifecycleScope.launch { pollPhoneReachability(applicationContext) }
        setContent { FleetRoot() }
    }
}

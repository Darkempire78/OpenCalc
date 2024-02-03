package com.darkempire78.opencalculator

import android.content.Intent
import android.service.quicksettings.TileService

class MyTileService: TileService() {
    companion object {
        private const val BROADCAST_ACTION = "darkempire78.opencalculator.action.START_ACTIVITY"
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        val intent = Intent(BROADCAST_ACTION)
        sendBroadcast(intent)
    }
}

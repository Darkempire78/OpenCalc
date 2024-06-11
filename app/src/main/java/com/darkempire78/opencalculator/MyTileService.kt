package com.darkempire78.opencalculator

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class MyTileService : TileService() {

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        // Intent to launch MainActivity
        val intent = Intent(this, MainActivity::class.java)
        // Create a PendingIntent from the intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Check the SDK version to determine which method to use
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            // For older versions, convert PendingIntent to Intent and start the activity
            val newIntent = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
        }
    }
}

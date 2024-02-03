package com.darkempire78.opencalculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyTileReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(newIntent)
    }
}

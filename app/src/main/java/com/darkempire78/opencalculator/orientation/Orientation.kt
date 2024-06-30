package com.darkempire78.opencalculator.orientation

import android.content.Context
import android.content.pm.ActivityInfo
import com.darkempire78.opencalculator.R

enum class Orientation(val resId: Int, val activityOrientation: Int) {
	SYSTEM(
		R.string.orientation_system,
		ActivityInfo.SCREEN_ORIENTATION_USER
	),
	PORTRAIT(
		R.string.orientation_portrait,
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
	),
	LANDSCAPE(R.string.orientation_landscape, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

	companion object {
		fun getByOrdinal(ordinal: Int): Orientation =
			Orientation.entries.first { it.ordinal == ordinal }

		fun getNamesFormResources(context: Context) =
			Orientation.entries.map { context.getString(it.resId) }.toTypedArray()
	}

}



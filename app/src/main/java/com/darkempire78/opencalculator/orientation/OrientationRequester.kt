package com.darkempire78.opencalculator.orientation

import android.app.Activity
import com.darkempire78.opencalculator.MyPreferences

object OrientationRequester {
	fun requestOrientation(activity: Activity) {
		activity.requestedOrientation =
			Orientation.getByOrdinal(MyPreferences(activity.applicationContext).orientation).activityOrientation
	}
}
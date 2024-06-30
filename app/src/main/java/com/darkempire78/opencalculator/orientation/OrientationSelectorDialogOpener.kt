package com.darkempire78.opencalculator.orientation

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.darkempire78.opencalculator.MyPreferences
import com.darkempire78.opencalculator.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object OrientationSelectorDialogOpener {

	fun openDialog(context: Context) {

		val preferences = MyPreferences(context)

		val builder = MaterialAlertDialogBuilder(context)
		builder.background = ContextCompat.getDrawable(context, R.drawable.rounded)

		builder.setSingleChoiceItems(
			Orientation.getNamesFormResources(context),
			preferences.orientation
		) { dialog, which ->
			preferences.orientation = which
			dialog.dismiss()
			reloadActivity(context)
		}
		val dialog = builder.create()
		dialog.show()
	}

	private fun reloadActivity(context: Context) {
		(context as Activity).finish()
		startActivity(context, context.intent, null)
	}
}
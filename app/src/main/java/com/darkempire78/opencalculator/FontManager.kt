package com.darkempire78.opencalculator

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView

class FontManager(context: Context) {
    val context: Context = context.applicationContext
    fun applyFont(application: Application) {
        val fontName = MyPreferences(context).appFont
        if (fontName != null && fontName != "Default") {
            val typeface = Typeface.createFromAsset(context.assets, "font/$fontName.ttf")
            // Apply the typeface to all activities
            application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    activity.findViewById<View>(android.R.id.content).viewTreeObserver
                        .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
                                applyFontToViewGroup(rootView, typeface)
                                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                        })
                }
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })
        }
    }

    private fun applyFontToViewGroup(viewGroup: ViewGroup, typeface: Typeface) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                applyFontToViewGroup(child, typeface)
            } else if (child is TextView) {
                child.typeface = typeface
            }
        }
    }
}
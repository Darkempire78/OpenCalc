package com.darkempire78.opencalculator

import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTests {

    @Test
    fun testLandscapeMainActivity(){
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }
        }
    }

    @Test
    fun testPortraitMainActivity(){
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        }
    }

    @Test
    fun testRecreateMainActivity(){
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()
        }
    }
}

package com.darkempire78.opencalculator

import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.darkempire78.opencalculator.activities.MainActivity
import com.darkempire78.opencalculator.calculator.parser.Expression
import junit.framework.TestCase.assertEquals
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
    fun testLogBase2Parsing() {
        val expression = Expression()
        val cleanExpression = expression.getCleanExpression("log₂(8)", ".", ",")
        assertEquals("log(8)/log(2)", cleanExpression)
    }


    @Test
    fun testRecreateMainActivity(){
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()
        }
    }
}

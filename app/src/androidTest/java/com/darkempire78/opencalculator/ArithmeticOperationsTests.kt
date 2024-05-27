package com.darkempire78.opencalculator

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ArithmeticOperationsTests {

    @Test
    fun testAdditionWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.oneButton)).perform(click())
            onView(withId(R.id.addButton)).perform(click())
            onView(withId(R.id.oneButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("2")))
        }
    }

    @Test
    fun testSubtractionWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nineButton)).perform(click())
            onView(withId(R.id.subtractButton)).perform(click())
            onView(withId(R.id.threeButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("6")))

        }
    }

    @Test
    fun testMultiplicationWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.multiplyButton)).perform(click())
            onView(withId(R.id.threeButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("6")))
        }
    }

    @Test
    fun testDivisionWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.divideButton)).perform(click())
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("3")))
        }
    }

    @Test
    fun testAdditionWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.addButton)).perform(click())
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("8.6")))
        }
    }

    @Test
    fun testSubtractionWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nineButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.nineButton)).perform(click())
            onView(withId(R.id.subtractButton)).perform(click())
            onView(withId(R.id.threeButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("6.9")))
        }
    }

    @Test
    fun testMultiplicationWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.fiveButton)).perform(click())
            onView(withId(R.id.multiplyButton)).perform(click())
            onView(withId(R.id.threeButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("7.5")))
        }
    }

    @Test
    fun testDivisionWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.fiveButton)).perform(click())
            onView(withId(R.id.divideButton)).perform(click())
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("3.25")))
        }
    }

    @Test
    fun testExponentWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.exponentButton)).perform(click())
            onView(withId(R.id.threeButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("8")))
        }
    }

    @Test
    fun testExponentWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.fiveButton)).perform(click())
            onView(withId(R.id.exponentButton)).perform(click())
            onView(withId(R.id.twoButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("6.25")))
        }
    }

    @Test
    fun testSquareRootWithIntegers() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.squareButton)).perform(click())
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("2.4494897428")))
        }
    }

    @Test
    fun testSquareRootWithFloats() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.squareButton)).perform(click())
            onView(withId(R.id.sixButton)).perform(click())
            onView(withId(R.id.pointButton)).perform(click())
            onView(withId(R.id.fiveButton)).perform(click())
            onView(withId(R.id.resultDisplay)).check(matches(withText("2.5495097568")))
        }
    }


}

package com.sitereports.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MainActivityTest {
    private val composeRule = createAndroidComposeRule<MainActivity>()
    private val clearDatabaseRule = TestRule { base: Statement, _: Description ->
        object : Statement() {
            override fun evaluate() {
                InstrumentationRegistry.getInstrumentation().targetContext
                    .deleteDatabase("daily-site-reports.db")
                base.evaluate()
            }
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(clearDatabaseRule).around(composeRule)

    @Test
    fun freshInstallShowsEmptyStateAndBottomNavigationWorks() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("No units yet")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("No units yet").assertIsDisplayed()
        composeRule.onNodeWithText("Reports").performClick()
        composeRule.onNodeWithText("MY REPORTS").assertIsDisplayed()
    }

    @Test
    fun userCanAddFirstUnit() {
        composeRule.onNodeWithText("Add Unit").performClick()
        composeRule.onNodeWithText("Block/Lot").performTextInput("B01L01")
        composeRule.onNodeWithText("Project").performTextInput("Sample Project")
        composeRule.onNodeWithText("Location").performTextInput("Sample Location")
        composeRule.onNodeWithText("Add").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("B01L01")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("B01L01").assertIsDisplayed()
    }
}

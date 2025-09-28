package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordShortcutsTest : BaseUiTest() {

    @Test
    fun actionVisibility() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRecord(name)
        calendar.timeInMillis = System.currentTimeMillis()
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
            timeEnded = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
        )
        testUtils.addRunningRecord(name)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        Thread.sleep(1000)

        // Running record - shown
        tryAction {
            longClickOnView(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
            )
        }
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Record - shown
        NavUtils.openRecordsScreen()
        clickOnView(
            allOf(withId(baseR.id.viewRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
        )
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Untracked - shown
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()
    }

    @Test
    fun add() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullRecordName = "$name - $tag"
        val fullShortcutName = "$name - $tag - $comment"

        // Setup
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = name,
            tagNames = listOf(tag),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        clickOnViewWithText(fullRecordName)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_shortcut)

        // Check shortcut
        NavUtils.openRunningRecordsScreen()
        checkShortcut(fullShortcutName)

        // Delete
        longClickOnView(withText(fullShortcutName))
        clickOnViewWithText(R.string.ok)
        checkViewDoesNotExist(withText(fullShortcutName))
    }

    @Test
    fun start() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullRecordName = "$name - $tag"
        val fullShortcutName = "$name - $tag - $comment"

        // Setup
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addShortcut(typeName = name, tagNames = listOf(tag), comment = comment)
        Thread.sleep(1000)

        // Start
        tryAction { checkShortcut(fullShortcutName) }
        clickOnViewWithText(fullShortcutName)
        checkRunningRecord(fullRecordName, comment)
    }

    @Suppress("SameParameterValue")
    private fun checkShortcut(
        name: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordShortcutItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRunningRecord(
        name: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }
}

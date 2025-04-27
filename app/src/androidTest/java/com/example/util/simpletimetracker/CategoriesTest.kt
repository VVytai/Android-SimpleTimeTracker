package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_categories.R as categoriesR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CategoriesTest : BaseUiTest() {

    @Test
    fun filtering() {
        val type1 = "type1"
        val type2 = "type2"
        val type3 = "type3"
        val type4 = "type4"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"
        val category1 = "category1"
        val category2 = "category2"
        val category3 = "category3"

        // Add data
        testUtils.addCategory(category1)
        testUtils.addCategory(category2)
        testUtils.addCategory(category3)
        testUtils.addActivity(type1)
        testUtils.addActivity(type2, categories = listOf(category2))
        testUtils.addActivity(type3, categories = listOf(category3))
        testUtils.addActivity(type4, categories = listOf(category2, category3))
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2, typeName = type2)
        testUtils.addRecordTag(tag3, typeName = type3)

        // Check
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkTagVisible(tag1, tag2, tag3, category1, category2, category3)

        longClickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(type1)
        clickOnViewWithText(R.string.change_record_save)
        checkTagNotVisible(tag1, tag2, tag3, category1, category2, category3)

        longClickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(type1)
        clickOnViewWithText(type2)
        clickOnViewWithText(R.string.change_record_save)
        checkTagVisible(tag2, category2)
        checkTagNotVisible(tag1, tag3, category1, category3)

        longClickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(type2)
        clickOnViewWithText(type3)
        clickOnViewWithText(R.string.change_record_save)
        checkTagVisible(tag3, category3)
        checkTagNotVisible(tag1, tag2, category1, category2)

        longClickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(type2)
        clickOnViewWithText(R.string.change_record_save)
        checkTagVisible(tag2, tag3, category2, category3)
        checkTagNotVisible(tag1, category1)

        longClickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(type2)
        clickOnViewWithText(type3)
        clickOnViewWithText(R.string.change_record_save)
        checkTagVisible(tag1, tag2, tag3, category1, category2, category3)
    }

    @Test
    fun search() {
        val tag1 = "tag1"
        val tag2 = "tag2 test2"
        val tag3 = "tag3 test3"
        val category1 = "category1"
        val category2 = "category2 test2"
        val category3 = "category3 test3"

        // Add data
        testUtils.addCategory(category1)
        testUtils.addCategory(category2)
        testUtils.addCategory(category3)
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)
        testUtils.addRecordTag(tag3)

        // Check
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkTagVisible(tag1, tag2, tag3, category1, category2, category3)

        // Search
        clickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(R.string.enable_search_hint)

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, tag1)
        tryAction {
            checkTagVisible(tag1)
            checkTagNotVisible(tag2, tag3, category1, category2, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, tag2)
        tryAction {
            checkTagVisible(tag2)
            checkTagNotVisible(tag1, tag3, category1, category2, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, category1)
        tryAction {
            checkTagVisible(category1)
            checkTagNotVisible(tag1, tag2, tag3, category2, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, category2)
        tryAction {
            checkTagVisible(category2)
            checkTagNotVisible(tag1, tag2, tag3, category1, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, "1")
        tryAction {
            checkTagVisible(tag1, category1)
            checkTagNotVisible(tag2, tag3, category2, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, "test2")
        tryAction {
            checkTagVisible(tag2, category2)
            checkTagNotVisible(tag1, tag3, category1, category3)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, "test")
        tryAction {
            checkTagVisible(tag2, tag3, category2, category3)
            checkTagNotVisible(tag1, category1)
        }

        typeTextIntoView(categoriesR.id.etCategoriesSearchField, "something")
        tryAction {
            checkTagNotVisible(tag1, tag2, tag3, category1, category2, category3)
        }

        // Disable
        clickOnViewWithId(categoriesR.id.btnCategoriesOptions)
        clickOnViewWithText(R.string.enable_search_hint)
        tryAction {
            checkViewIsNotDisplayed(withId(categoriesR.id.etCategoriesSearchField))
            checkTagVisible(tag1, tag2, tag3, category1, category2, category3)
        }
    }

    private fun checkTagVisible(vararg name: String) {
        name.forEach {
            checkViewIsDisplayed(
                allOf(
                    withId(dialogsR.id.viewCategoryItem),
                    hasDescendant(withText(it)),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }

    private fun checkTagNotVisible(vararg name: String) {
        name.forEach {
            checkViewDoesNotExist(
                allOf(
                    withId(dialogsR.id.viewCategoryItem),
                    hasDescendant(withText(it)),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }
}

package com.example.util.simpletimetracker.feature_tag_selection.interactor

import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryShowSuggestionsViewData
import com.example.util.simpletimetracker.feature_base_adapter.commentField.CommentFieldViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_tag_selection.R
import com.example.util.simpletimetracker.feature_tag_selection.adapter.RecordTagSelectionTextViewData
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import javax.inject.Inject

class RecordTagSelectionViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
) {

    suspend fun getViewData(
        extra: RecordTagSelectionParams,
        selectedTags: List<Long>,
        showAllTags: Boolean,
        comment: String,
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        val typeId = extra.typeId
        val closeAfterOneTagSelected = prefsInteractor.getRecordTagSelectionCloseAfterOne()
        val showSuggestions = prefsInteractor.getIsCommentSelectionSuggestionsEnabled()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val shouldShowCommentSelection = RecordTagSelectionParams.Field.Comment in extra.fields
        val shouldShowTagSelection = RecordTagSelectionParams.Field.Tags in extra.fields

        val result: MutableList<ViewHolderType> = mutableListOf()

        if (shouldShowCommentSelection) {
            result += RecordTagSelectionTextViewData(
                text = resourceRepo.getString(R.string.change_record_comment_field),
            )

            result += CommentFieldViewData(
                id = 1L, // Only one at the time.
                text = if (fromCommentChange) null else comment,
                marginTopDp = 0,
                marginHorizontal = resourceRepo.getDimenInDp(R.dimen.edit_screen_margin_horizontal),
            )

            result += CategoryShowSuggestionsViewData(
                name = resourceRepo.getString(R.string.change_record_last_comments_hint),
                color = if (showSuggestions) {
                    resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme)
                } else {
                    resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme)
                },
            )

            if (showSuggestions) {
                result += recordCommentSearchViewDataInteractor.getSearchData(comment)
                result += recordCommentSearchViewDataInteractor.getFavouriteData()
                result += recordCommentSearchViewDataInteractor.getLastCommentsData(typeId)
            }
        }

        if (shouldShowTagSelection) {
            if (shouldShowCommentSelection) {
                result += EmptySpaceViewData(
                    id = "record_tag_selection_divider_space".hashCode().toLong(),
                    width = EmptySpaceViewData.ViewDimension.MatchParent,
                    height = EmptySpaceViewData.ViewDimension.ExactSizeDp(16),
                )
            }

            result += RecordTagSelectionTextViewData(
                text = resourceRepo.getString(R.string.record_tag_selection_hint),
            )

            result += EmptySpaceViewData(
                id = "record_tag_selection_tags_list_divider_space".hashCode().toLong(),
                width = EmptySpaceViewData.ViewDimension.MatchParent,
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(8),
            )

            result += recordTagViewDataInteractor.getViewData(
                selectedTags = selectedTags,
                typeId = if (showAllTags) null else typeId,
                multipleChoiceAvailable = !closeAfterOneTagSelected,
                showAddButton = false,
                showArchived = false,
                showUntaggedButton = true,
                showAllTagsButton = !showAllTags,
            ).data
        }

        result += EmptySpaceViewData(
            id = "record_tag_selection_end_space".hashCode().toLong(),
            width = EmptySpaceViewData.ViewDimension.MatchParent,
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(8),
        )

        return result
    }
}
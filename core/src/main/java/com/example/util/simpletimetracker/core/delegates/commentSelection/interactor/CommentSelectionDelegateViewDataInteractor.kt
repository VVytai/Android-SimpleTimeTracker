package com.example.util.simpletimetracker.core.delegates.commentSelection.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.RecordTypeToFavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.commentChangeField.ChangeRecordCommentFieldViewData
import javax.inject.Inject
import kotlin.text.get

class CommentSelectionDelegateViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val recordTypeToFavouriteCommentInteractor: RecordTypeToFavouriteCommentInteractor,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
) {

    // TODO replace fav to text button?
    suspend fun getCommentsViewData(
        comment: String,
        typeId: Long,
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        val items = mutableListOf<ViewHolderType>()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val favouriteComment = favouriteCommentInteractor.get(comment)
        val isFavourite = if (favouriteComment != null) {
            recordTypeToFavouriteCommentInteractor.filterFavourites(
                typeId = typeId,
                comments = listOf(favouriteComment),
            ).isNotEmpty()
        } else {
            false
        }

        ChangeRecordCommentFieldViewData(
            // Only one at the time.
            id = 1L,
            // Do not update text if update coming from typing.
            text = if (fromCommentChange) null else comment,
            iconColor = if (isFavourite) {
                resourceRepo.getThemedAttr(R.attr.colorAccent, isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
        ).let(items::add)

        items += recordCommentSearchViewDataInteractor.getViewData(
            comment = comment,
            typeId = typeId,
        )

        return items
    }
}
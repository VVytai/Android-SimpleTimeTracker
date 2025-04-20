package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import javax.inject.Inject

class RecordCommentSearchViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun getSearchData(
        comment: String,
    ): List<ViewHolderType> {
        return if (comment.isNotEmpty()) {
            recordInteractor.searchComment(comment)
                .asSequence()
                .sortedByDescending { it.timeStarted }
                .map { it.comment }
                .toSet()
                .mapNotNull {
                    if (it == comment) return@mapNotNull null
                    RecordCommentViewData.Last(it)
                }
                .takeUnless { it.isEmpty() }
                ?.let {
                    HintViewData(
                        text = resourceRepo.getString(R.string.change_record_similar_comments_hint),
                    ).let(::listOf) + it
                }.orEmpty()
        } else {
            emptyList()
        }
    }

    suspend fun getFavouriteData(

    ): List<ViewHolderType> {
        return favouriteCommentInteractor.getAll()
            .map { RecordCommentViewData.Favourite(it.comment) }
            .takeUnless { it.isEmpty() }
            ?.let {
                HintViewData(
                    text = resourceRepo.getString(R.string.change_record_favourite_comments_hint),
                ).let(::listOf) + it
            }.orEmpty()
    }

    suspend fun getLastCommentsData(
        typeId: Long,
    ): List<ViewHolderType> {
        data class Data(val timeStarted: Long, val comment: String)

        val records = recordInteractor.getByTypeWithAnyComment(listOf(typeId))
            .map { Data(it.timeStarted, it.comment) }
        val runningRecords = runningRecordInteractor.getAll()
            .filter { it.id == typeId && it.comment.isNotEmpty() }
            .map { Data(it.timeStarted, it.comment) }

        return (records + runningRecords)
            .asSequence()
            .sortedByDescending { it.timeStarted }
            .map { it.comment }
            .toSet()
            .take(LAST_COMMENTS_TO_SHOW)
            .map { RecordCommentViewData.Last(it) }
            .takeUnless { it.isEmpty() }
            ?.let {
                HintViewData(
                    text = resourceRepo.getString(R.string.change_record_last_comments_hint),
                ).let(::listOf) + it
            }.orEmpty()
    }

    companion object {
        private const val LAST_COMMENTS_TO_SHOW = 20
    }
}
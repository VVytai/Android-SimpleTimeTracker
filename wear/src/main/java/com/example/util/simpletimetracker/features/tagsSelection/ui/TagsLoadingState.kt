package com.example.util.simpletimetracker.features.tagsSelection.ui

import com.example.util.simpletimetracker.features.tagsSelection.screen.TagListState

sealed interface TagsLoadingState {
    object NotLoading : TagsLoadingState

    data class LoadingTag(
        val tagId: Long,
    ) : TagsLoadingState

    data class LoadingButton(
        val buttonType: TagListState.Item.ButtonType,
    ) : TagsLoadingState
}
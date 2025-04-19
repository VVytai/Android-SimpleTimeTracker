package com.example.util.simpletimetracker.feature_archive.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.model.ArchiveOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class ArchiveOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun map(): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        result += OptionsListParams.Item(
            id = ArchiveOptionsListItem.EnabledSearch,
            text = resourceRepo.getString(R.string.enable_search_hint),
            icon = R.drawable.search,
            isIconCheckVisible = prefsInteractor.getIsArchiveSearchEnabled(),
        )

        return result
    }
}
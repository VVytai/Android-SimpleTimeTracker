package com.example.util.simpletimetracker.feature_categories.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.model.CategoriesOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class CategoriesOptionsListMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun map(
        selectedIds: List<Long>,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        result += OptionsListParams.Item(
            id = CategoriesOptionsListItem.Help,
            text = resourceRepo.getString(R.string.help),
            icon = R.drawable.unknown,
            isIconCheckVisible = false,
        )

        result += OptionsListParams.Item(
            id = CategoriesOptionsListItem.EnabledSearch,
            text = resourceRepo.getString(R.string.enable_search_hint),
            icon = R.drawable.search,
            isIconCheckVisible = prefsInteractor.getIsCategoriesSearchEnabled(),
        )

        result += OptionsListParams.Item(
            id = CategoriesOptionsListItem.Filter,
            text = resourceRepo.getString(R.string.chart_filter_hint),
            icon = R.drawable.filter,
            isIconCheckVisible = selectedIds.isNotEmpty(),
        )

        return result
    }
}
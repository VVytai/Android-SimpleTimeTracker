package com.example.util.simpletimetracker.data_local.recordsFilter

import com.example.util.simpletimetracker.data_local.daysOfWeek.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.record.model.FavouriteRecordsFilter
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class FavouriteRecordsFilterDataLocalMapper @Inject constructor(
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
) {

    fun map(dbo: FavouriteRecordsFilterDBO): FavouriteRecordsFilter? {
        return FavouriteRecordsFilter(
            id = dbo.main.id,
            filter = dbo.filters
                .mapNotNull(::map)
                .takeIf { it.isNotEmpty() } ?: return null,
        )
    }

    private fun map(dbo: FavouriteRecordsFilterDBO.FilterWithDataDBO): RecordsFilter? {
        val range = dbo.filter.range?.let {
            Range(
                timeStarted = it.rangeTimeStarted,
                timeEnded = it.rangeTimeEnded,
            )
        }
        val rangeLength = dbo.filter.rangeLength?.let { data ->
            when (data.rangeType) {
                0L -> RangeLength.Day
                1L -> RangeLength.Week
                2L -> RangeLength.Month
                3L -> RangeLength.Year
                4L -> RangeLength.All
                5L -> data.customRange?.let {
                    val range = Range(timeStarted = it.rangeTimeStarted, timeEnded = it.rangeTimeEnded)
                    RangeLength.Custom(range)
                }
                6L -> data.lastDays?.let { data ->
                    RangeLength.Last(data.toInt())
                }
                else -> null
            }
        }
        val rangeLengthPosition = dbo.filter.rangeLength?.position?.toInt()
        val daysOfWeek = dbo.filter.daysOfWeek
            ?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek)
            ?.toList()
        val commentItems = dbo.commentItems.mapNotNull {
            when (it.type) {
                0L -> RecordsFilter.CommentItem.NoComment
                1L -> RecordsFilter.CommentItem.AnyComment
                2L -> RecordsFilter.CommentItem.Comment(it.text ?: return@mapNotNull null)
                else -> return@mapNotNull null
            }
        }.takeIf { it.isNotEmpty() }
        val duplicationItems = dbo.duplicationItems.mapNotNull {
            when (it.type) {
                0L -> RecordsFilter.DuplicationsItem.SameActivity
                1L -> RecordsFilter.DuplicationsItem.SameTimes
                else -> return@mapNotNull null
            }
        }.takeIf { it.isNotEmpty() }
        val manuallyFilteredItems = dbo.manuallyFilteredItems.mapNotNull { data ->
            val ids = data.itemIds
                ?.split(',')
                ?.mapNotNull(String::toLongOrNull)
            val id = ids?.firstOrNull()
            val range = data.range?.let {
                Range(timeStarted = it.rangeTimeStarted, timeEnded = it.rangeTimeEnded)
            }
            when (data.type) {
                0L -> RecordsFilter.ManuallyFilteredItem.Tracked(id ?: return@mapNotNull null)
                1L -> RecordsFilter.ManuallyFilteredItem.Running(id ?: return@mapNotNull null)
                2L -> RecordsFilter.ManuallyFilteredItem.Multitask(ids ?: return@mapNotNull null)
                3L -> RecordsFilter.ManuallyFilteredItem.Untracked(range ?: return@mapNotNull null)
                else -> return@mapNotNull null
            }
        }.takeIf { it.isNotEmpty() }

        val filter = when (dbo.filter.type) {
            0L -> RecordsFilter.Untracked
            1L -> RecordsFilter.Multitask
            2L -> {
                val selectedItems = dbo.commonItems
                    .filter { it.type == 0L && it.isSelected }
                    .mapNotNull { it.itemId }
                val filteredItems = dbo.commonItems
                    .filter { it.type == 0L && !it.isSelected }
                    .mapNotNull { it.itemId }

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Activity(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            3L -> {
                val selectedCategorizedItems = dbo.commonItems
                    .filter { it.type == 1L && it.isSelected }
                    .mapNotNull { it.itemId }
                    .map { RecordsFilter.CategoryItem.Categorized(it) }
                val selectedUncategorizedItems = dbo.commonItems
                    .filter { it.type == 2L && it.isSelected }
                    .map { RecordsFilter.CategoryItem.Uncategorized }
                val selectedItems = selectedCategorizedItems + selectedUncategorizedItems
                val filteredCategorizedItems = dbo.commonItems
                    .filter { it.type == 1L && !it.isSelected }
                    .mapNotNull { it.itemId }
                    .map { RecordsFilter.CategoryItem.Categorized(it) }
                val filteredUncategorizedItems = dbo.commonItems
                    .filter { it.type == 2L && !it.isSelected }
                    .map { RecordsFilter.CategoryItem.Uncategorized }
                val filteredItems = filteredCategorizedItems + filteredUncategorizedItems

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Category(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            4L -> {
                val selectedTaggedItems = dbo.commonItems
                    .filter { it.type == 3L && it.isSelected }
                    .mapNotNull { it.itemId }
                    .map { RecordsFilter.TagItem.Tagged(it) }
                val selectedUntaggedItems = dbo.commonItems
                    .filter { it.type == 4L && it.isSelected }
                    .map { RecordsFilter.TagItem.Untagged }
                val selectedItems = selectedTaggedItems + selectedUntaggedItems
                val filteredTaggedItems = dbo.commonItems
                    .filter { it.type == 3L && !it.isSelected }
                    .mapNotNull { it.itemId }
                    .map { RecordsFilter.TagItem.Tagged(it) }
                val filteredUntaggedItems = dbo.commonItems
                    .filter { it.type == 4L && !it.isSelected }
                    .map { RecordsFilter.TagItem.Untagged }
                val filteredItems = filteredTaggedItems + filteredUntaggedItems

                if (selectedItems.isNotEmpty() || filteredItems.isNotEmpty()) {
                    RecordsFilter.Tags(selected = selectedItems, filtered = filteredItems)
                } else {
                    return null
                }
            }
            5L -> RecordsFilter.Comment(commentItems ?: return null)
            6L -> RecordsFilter.Date(rangeLength ?: return null, rangeLengthPosition ?: return null)
            7L -> RecordsFilter.ManuallyFiltered(manuallyFilteredItems ?: return null)
            8L -> RecordsFilter.DaysOfWeek(daysOfWeek ?: return null)
            9L -> RecordsFilter.TimeOfDay(range ?: return null)
            10L -> RecordsFilter.Duration(range ?: return null)
            11L -> RecordsFilter.Duplications(duplicationItems ?: return null)
            else -> return null
        }

        return filter
    }

    fun map(domain: FavouriteRecordsFilter): FavouriteRecordsFilterDBO {
        return FavouriteRecordsFilterDBO(
            main = FavouriteRecordsFilterDBO.MainDBO(
                id = domain.id,
            ),
            filters = domain.filter.map(::map),
        )
    }

    private fun map(domain: RecordsFilter): FavouriteRecordsFilterDBO.FilterWithDataDBO {
        val range = when (domain) {
            is RecordsFilter.TimeOfDay -> domain.range
            is RecordsFilter.Duration -> domain.range
            else -> null
        }?.let {
            FavouriteRecordsFilterDBO.RangeDBO(
                rangeTimeStarted = it.timeStarted,
                rangeTimeEnded = it.timeEnded,
            )
        }
        val rangeLength = (domain as? RecordsFilter.Date)?.let { data ->
            FavouriteRecordsFilterDBO.RangeLengthDBO(
                rangeType = when (data.range) {
                    is RangeLength.Day -> 0L
                    is RangeLength.Week -> 1L
                    is RangeLength.Month -> 2L
                    is RangeLength.Year -> 3L
                    is RangeLength.All -> 4L
                    is RangeLength.Custom -> 5L
                    is RangeLength.Last -> 6L
                },
                customRange = (data.range as? RangeLength.Custom)?.range?.let {
                    FavouriteRecordsFilterDBO.RangeDBO(
                        rangeTimeStarted = it.timeStarted,
                        rangeTimeEnded = it.timeEnded,
                    )
                },
                lastDays = (data.range as? RangeLength.Last)?.days?.toLong(),
                position = data.position.toLong(),
            )
        }
        val daysOfWeek = (domain as? RecordsFilter.DaysOfWeek)?.items?.toSet()
        val items = when (domain) {
            is RecordsFilter.Activity -> {
                val data = domain.selected.map { it to true } +
                    domain.filtered.map { it to false }
                data.map { (itemId, isSelected) ->
                    FavouriteRecordsFilterDBO.CommonItemDBO(
                        id = 0L,
                        filterId = 0L,
                        type = 0L,
                        isSelected = isSelected,
                        itemId = itemId,
                    )
                }
            }
            is RecordsFilter.Category -> {
                val data = domain.selected.map { it to true } +
                    domain.filtered.map { it to false }
                data.map { (item, isSelected) ->
                    FavouriteRecordsFilterDBO.CommonItemDBO(
                        id = 0L,
                        filterId = 0L,
                        type = when (item) {
                            is RecordsFilter.CategoryItem.Categorized -> 1L
                            is RecordsFilter.CategoryItem.Uncategorized -> 2L
                        },
                        isSelected = isSelected,
                        itemId = (item as? RecordsFilter.CategoryItem.Categorized)?.categoryId,
                    )
                }
            }
            is RecordsFilter.Tags -> {
                val data = domain.selected.map { it to true } +
                    domain.filtered.map { it to false }
                data.map { (item, isSelected) ->
                    FavouriteRecordsFilterDBO.CommonItemDBO(
                        id = 0L,
                        filterId = 0L,
                        type = when (item) {
                            is RecordsFilter.TagItem.Tagged -> 3L
                            is RecordsFilter.TagItem.Untagged -> 4L
                        },
                        isSelected = isSelected,
                        itemId = (item as? RecordsFilter.TagItem.Tagged)?.tagId,
                    )
                }
            }
            else -> emptyList()
        }
        val commentItems = (domain as? RecordsFilter.Comment)?.items?.map {
            FavouriteRecordsFilterDBO.CommentItemDBO(
                id = 0L,
                filterId = 0L,
                type = when (it) {
                    is RecordsFilter.CommentItem.NoComment -> 0L
                    is RecordsFilter.CommentItem.AnyComment -> 1L
                    is RecordsFilter.CommentItem.Comment -> 2L
                },
                text = (it as? RecordsFilter.CommentItem.Comment)?.text,
            )
        }.orEmpty()
        val duplicationItems = (domain as? RecordsFilter.Duplications)?.items?.map {
            FavouriteRecordsFilterDBO.DuplicationItemDBO(
                id = 0L,
                filterId = 0L,
                type = when (it) {
                    is RecordsFilter.DuplicationsItem.SameActivity -> 0L
                    is RecordsFilter.DuplicationsItem.SameTimes -> 1L
                },
            )
        }.orEmpty()
        val manuallyFilteredItems = (domain as? RecordsFilter.ManuallyFiltered)?.items?.map { data ->
            FavouriteRecordsFilterDBO.ManuallyFilteredItemDBO(
                id = 0L,
                filterId = 0L,
                type = when (data) {
                    is RecordsFilter.ManuallyFilteredItem.Tracked -> 0L
                    is RecordsFilter.ManuallyFilteredItem.Running -> 1L
                    is RecordsFilter.ManuallyFilteredItem.Multitask -> 2L
                    is RecordsFilter.ManuallyFilteredItem.Untracked -> 3L
                },
                itemIds = when (data) {
                    is RecordsFilter.ManuallyFilteredItem.Tracked -> listOf(data.id)
                    is RecordsFilter.ManuallyFilteredItem.Running -> listOf(data.id)
                    is RecordsFilter.ManuallyFilteredItem.Multitask -> listOf(data.ids)
                    is RecordsFilter.ManuallyFilteredItem.Untracked -> null
                }?.joinToString(separator = ","),
                range = (data as? RecordsFilter.ManuallyFilteredItem.Untracked)?.range?.let {
                    FavouriteRecordsFilterDBO.RangeDBO(
                        rangeTimeStarted = it.timeStarted,
                        rangeTimeEnded = it.timeEnded,
                    )
                },
            )
        }.orEmpty()

        val filter = FavouriteRecordsFilterDBO.FilterDBO(
            id = 0L,
            ownerId = 0L,
            type = when (domain) {
                is RecordsFilter.Untracked -> 0L
                is RecordsFilter.Multitask -> 1L
                is RecordsFilter.Activity -> 2L
                is RecordsFilter.Category -> 3L
                is RecordsFilter.Tags -> 4L
                is RecordsFilter.Comment -> 5L
                is RecordsFilter.Date -> 6L
                is RecordsFilter.ManuallyFiltered -> 7L
                is RecordsFilter.DaysOfWeek -> 8L
                is RecordsFilter.TimeOfDay -> 9L
                is RecordsFilter.Duration -> 10L
                is RecordsFilter.Duplications -> 11L
            },
            range = range,
            rangeLength = rangeLength,
            daysOfWeek = daysOfWeek?.let(daysOfWeekDataLocalMapper::mapDaysOfWeek),
        )

        return FavouriteRecordsFilterDBO.FilterWithDataDBO(
            filter = filter,
            commonItems = items,
            commentItems = commentItems,
            duplicationItems = duplicationItems,
            manuallyFilteredItems = manuallyFilteredItems,
        )
    }
}
package com.example.util.simpletimetracker.data_local.recordsFilter

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeDBO

data class FavouriteRecordsFilterDBO(
    @Embedded
    val main: MainDBO,

    @Relation(
        entity = FilterDBO::class,
        parentColumn = "id",
        entityColumn = "owner_id",
    )
    val filters: List<FilterWithDataDBO>,
) {

    @Entity(tableName = "favouriteRecordFilters")
    data class MainDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,
    )

    data class FilterWithDataDBO(
        @Embedded
        val filter: FilterDBO,

        @Relation(
            parentColumn = "id",
            entityColumn = "filter_id",
        )
        val commonItems: List<CommonItemDBO>,

        @Relation(
            parentColumn = "id",
            entityColumn = "filter_id",
        )
        val commentItems: List<CommentItemDBO>,

        @Relation(
            parentColumn = "id",
            entityColumn = "filter_id",
        )
        val duplicationItems: List<DuplicationItemDBO>,

        @Relation(
            parentColumn = "id",
            entityColumn = "filter_id",
        )
        val manuallyFilteredItems: List<ManuallyFilteredItemDBO>,
    )

    @Entity(
        tableName = "favouriteRecordFilter",
        foreignKeys = [
            ForeignKey(
                entity = MainDBO::class,
                parentColumns = ["id"],
                childColumns = ["owner_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class FilterDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "owner_id")
        val ownerId: Long,

        @ColumnInfo(name = "type")
        val type: Long,

        @Embedded(prefix = "range_")
        val range: RangeDBO?,

        @Embedded(prefix = "range_length_")
        val rangeLength: RangeLengthDBO?,

        /**
         * How data is stored - see [RecordTypeDBO].
         */
        @ColumnInfo(name = "daysOfWeek")
        val daysOfWeek: String?,
    )

    @Entity(
        tableName = "favouriteRecordFiltersCommonItems",
        foreignKeys = [
            ForeignKey(
                entity = FilterDBO::class,
                parentColumns = ["id"],
                childColumns = ["filter_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class CommonItemDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "filter_id")
        val filterId: Long,

        @ColumnInfo(name = "is_selected")
        val isSelected: Boolean,

        @ColumnInfo(name = "type")
        val type: Long,

        @ColumnInfo(name = "item_id")
        val itemId: Long?,
    )

    @Entity(
        tableName = "favouriteRecordFiltersCommentItems",
        foreignKeys = [
            ForeignKey(
                entity = FilterDBO::class,
                parentColumns = ["id"],
                childColumns = ["filter_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class CommentItemDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "filter_id")
        val filterId: Long,

        @ColumnInfo(name = "type")
        val type: Long,

        @ColumnInfo(name = "text")
        val text: String?,
    )

    @Entity(
        tableName = "favouriteRecordFiltersDuplicationItems",
        foreignKeys = [
            ForeignKey(
                entity = FilterDBO::class,
                parentColumns = ["id"],
                childColumns = ["filter_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class DuplicationItemDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "filter_id")
        val filterId: Long,

        @ColumnInfo(name = "type")
        val type: Long,
    )

    @Entity(
        tableName = "favouriteRecordFiltersManuallyFilteredItems",
        foreignKeys = [
            ForeignKey(
                entity = FilterDBO::class,
                parentColumns = ["id"],
                childColumns = ["filter_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class ManuallyFilteredItemDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "filter_id")
        val filterId: Long,

        @ColumnInfo(name = "type")
        val type: Long,

        // Longs stored in string comma separated
        @ColumnInfo(name = "item_ids")
        val itemIds: String?,

        @Embedded(prefix = "range_")
        val range: RangeDBO?,
    )

    data class RangeDBO(
        @ColumnInfo(name = "time_started")
        val rangeTimeStarted: Long,

        @ColumnInfo(name = "time_ended")
        val rangeTimeEnded: Long,
    )

    data class RangeLengthDBO(
        @ColumnInfo(name = "type")
        val rangeType: Long,

        @Embedded(prefix = "custom_range_")
        val customRange: RangeDBO?,

        @ColumnInfo(name = "last_days")
        val lastDays: Long?,

        @ColumnInfo(name = "position")
        val position: Long,
    )
}
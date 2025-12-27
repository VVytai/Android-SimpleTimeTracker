package com.example.util.simpletimetracker.data_local.recordsFilter

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FavouriteRecordsFilterDao {

    @Transaction
    @Query("SELECT * FROM favouriteRecordFilters")
    suspend fun getAll(): List<FavouriteRecordsFilterDBO>

    @Transaction
    @Query("SELECT * FROM favouriteRecordFilters WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): FavouriteRecordsFilterDBO?

    @Transaction
    suspend fun insert(data: FavouriteRecordsFilterDBO) {
        val mainId = insertMain(data.main)

        data.filters.forEach { filter ->
            val filterId = insertFilter(filter.filter.copy(ownerId = mainId))
            insertItems(filter.commonItems.map { it.copy(filterId = filterId) })
            insertCommentItems(filter.commentItems.map { it.copy(filterId = filterId) })
            insertDuplicationItems(filter.duplicationItems.map { it.copy(filterId = filterId) })
            insertManuallyFiltered(filter.manuallyFilteredItems.map { it.copy(filterId = filterId) })
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMain(data: FavouriteRecordsFilterDBO.MainDBO): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilter(data: FavouriteRecordsFilterDBO.FilterDBO): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(data: List<FavouriteRecordsFilterDBO.CommonItemDBO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommentItems(data: List<FavouriteRecordsFilterDBO.CommentItemDBO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuplicationItems(data: List<FavouriteRecordsFilterDBO.DuplicationItemDBO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManuallyFiltered(data: List<FavouriteRecordsFilterDBO.ManuallyFilteredItemDBO>)

    @Delete
    suspend fun delete(data: FavouriteRecordsFilterDBO.MainDBO)

    @Query("DELETE FROM favouriteRecordFilters")
    suspend fun clear()
}
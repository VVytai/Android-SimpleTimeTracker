package com.example.util.simpletimetracker.data_local.recordShortcut

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecordShortcutDao {

    @Transaction
    @Query("SELECT * FROM recordShortcuts")
    suspend fun getAll(): List<RecordShortcutWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM recordShortcuts WHERE type_id IN (:typesIds)")
    suspend fun getByType(typesIds: List<Long>): List<RecordShortcutWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM recordShortcuts WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordShortcutWithRecordTagsDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordShortcutDBO): Long

    @Query("DELETE FROM recordShortcuts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordShortcuts WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM recordShortcuts")
    suspend fun clear()
}
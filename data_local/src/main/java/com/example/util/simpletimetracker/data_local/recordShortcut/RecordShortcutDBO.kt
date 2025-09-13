package com.example.util.simpletimetracker.data_local.recordShortcut

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordShortcuts")
data class RecordShortcutDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "type_id")
    val typeId: Long,

    @ColumnInfo(name = "comment")
    val comment: String,
)
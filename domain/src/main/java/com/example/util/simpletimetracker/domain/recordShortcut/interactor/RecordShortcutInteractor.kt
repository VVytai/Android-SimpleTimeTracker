package com.example.util.simpletimetracker.domain.recordShortcut.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordShortcut.repo.RecordShortcutRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class RecordShortcutInteractor @Inject constructor(
    private val repo: RecordShortcutRepo,
    private val recordShortcutToRecordTagRepo: RecordShortcutToRecordTagRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
) {

    suspend fun getAll(): List<RecordShortcut> {
        return sort(
            data = repo.getAll(),
            order = recordTypeInteractor.getAll(),
        )
    }

    suspend fun get(id: Long): RecordShortcut? {
        return repo.get(id)
    }

    suspend fun add(recordShortcut: RecordShortcut) {
        val addedId = repo.add(recordShortcut)
        updateTags(addedId, recordShortcut.tags)
    }

    suspend fun remove(id: Long) {
        recordShortcutToRecordTagRepo.removeAllByShortcutId(id)
        repo.remove(id)
    }

    private suspend fun updateTags(
        shortcutId: Long,
        tags: List<RecordBase.Tag>,
    ) {
        recordShortcutToRecordTagRepo.removeAllByShortcutId(shortcutId)
        recordShortcutToRecordTagRepo.addRecordTags(shortcutId, tags)
    }

    private fun sort(
        data: List<RecordShortcut>,
        order: List<RecordType>,
    ): List<RecordShortcut> {
        val orderMap = order
            .mapIndexed { index, recordType -> recordType.id to index }
            .toMap()

        return data.sortedBy { orderMap[it.typeId].orZero() }
    }
}
package com.example.util.simpletimetracker.domain.recordTag.interactor

import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import javax.inject.Inject

class AddTagToTypeIfNotExistMediator @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
) {

    suspend fun execute(
        typeId: Long,
        tagIds: List<Long>,
    ) {
        val allTags = recordTagInteractor.getAll().map(RecordTag::id)
        val typesToTags = recordTypeToTagInteractor.getAll()

        val currentTyped = typesToTags
            .mapNotNull { if (it.recordTypeId == typeId) it.tagId else null }
            .distinct()
        val typedTags = typesToTags
            .map { it.tagId }
            .distinct()
        val untyped = allTags.filter { it !in typedTags }

        val needToAdd = tagIds.filter { it !in currentTyped && it !in untyped }
        recordTypeToTagInteractor.addTags(typeId, needToAdd)
    }
}
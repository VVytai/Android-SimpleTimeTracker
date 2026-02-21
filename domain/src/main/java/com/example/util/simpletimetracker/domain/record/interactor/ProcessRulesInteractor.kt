package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.base.ResultContainer
import com.example.util.simpletimetracker.domain.base.SuspendLazy
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleProcessActionInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import javax.inject.Inject
import kotlin.collections.forEach

class ProcessRulesInteractor @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val complexRuleProcessActionInteractor: ComplexRuleProcessActionInteractor,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
) {

    suspend fun getRulesResultForStart(
        typeId: Long,
        timeStarted: Long,
        prevRecords: SuspendLazy<List<Record>>,
        retroactiveTrackingMode: Boolean,
    ): ComplexRuleProcessActionInteractor.Result {
        return if (
            retroactiveTrackingMode &&
            getPrevRecordToMergeWith(typeId, prevRecords) != null
        ) {
            // No need to check rules on merge.
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                disallowOnlyPreviousTypeIds = emptySet(),
                tags = emptyList(),
                tagIdsToSelectValueOnStart = emptySet(),
            )
        } else {
            processRules(
                typeId = typeId,
                timeStarted = timeStarted,
                prevRecords = prevRecords,
            )
        }
    }

    suspend fun getAllTags(
        typeId: Long,
        currentTags: List<RecordBase.Tag>,
        tagValuesFromRules: List<RecordBase.Tag>,
    ): List<RecordBase.Tag> {
        val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
        val merged = linkedMapOf<Long, RecordBase.Tag>()

        fun merge(tag: RecordBase.Tag) {
            val existing = merged[tag.tagId]
            // Tags with values takes precedence.
            if (existing == null || (existing.numericValue == null && tag.numericValue != null)) {
                merged[tag.tagId] = tag
            }
        }

        currentTags.forEach(::merge)
        defaultTags.map { RecordBase.Tag(tagId = it, numericValue = null) }.forEach(::merge)
        tagValuesFromRules.forEach(::merge)

        return merged.values.toList()
    }

    suspend fun getPrevRecordToMergeWith(
        typeId: Long,
        prevRecords: SuspendLazy<List<Record>>,
    ): Record? {
        return prevRecords().firstOrNull { it.typeId == typeId }
    }

    private suspend fun processRules(
        typeId: Long,
        timeStarted: Long,
        prevRecords: SuspendLazy<List<Record>>,
    ): ComplexRuleProcessActionInteractor.Result {
        // If no rules - no need to check them.
        return if (complexRuleProcessActionInteractor.hasRules()) {
            val currentRecords = runningRecordInteractor.getAll()
            val hasAnyRunningTimersOnTimeStarted = currentRecords.any {
                it.timeStarted <= timeStarted
            }
            val takeCurrentRecords = currentRecords.isNotEmpty() &&
                hasAnyRunningTimersOnTimeStarted

            // If no current records - check closest previous.
            val records = if (takeCurrentRecords) currentRecords else prevRecords()

            val currentTypeIds = records
                .map { it.typeIds }
                .flatten()
                .toSet()

            complexRuleProcessActionInteractor.processRules(
                timeStarted = timeStarted,
                startingTypeId = typeId,
                currentTypeIds = currentTypeIds,
            )
        } else {
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                disallowOnlyPreviousTypeIds = emptySet(),
                tags = emptyList(),
                tagIdsToSelectValueOnStart = emptySet(),
            )
        }
    }
}
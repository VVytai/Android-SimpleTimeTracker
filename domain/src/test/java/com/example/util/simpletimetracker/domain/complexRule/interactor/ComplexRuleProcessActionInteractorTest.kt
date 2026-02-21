package com.example.util.simpletimetracker.domain.complexRule.interactor

import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetCurrentDayInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ComplexRuleProcessActionInteractorTest {

    private val complexRuleInteractor: ComplexRuleInteractor = mock()
    private val getCurrentDayInteractor: GetCurrentDayInteractor = mock()

    private val subject = ComplexRuleProcessActionInteractor(
        complexRuleInteractor = complexRuleInteractor,
        getCurrentDayInteractor = getCurrentDayInteractor,
    )

    private val startingTypeId = 1L
    private val currentTypeIds = setOf(2L)
    private val currentDay = DayOfWeek.MONDAY

    @Before
    fun before(): Unit = runBlocking {
        `when`(getCurrentDayInteractor.execute(any())).thenReturn(currentDay)
    }

    @Test
    fun mergesSelectOnStart(): Unit = runBlocking {
        val rule1 = assignTagRule(
            actionAssignTagValues = listOf(tag(10L, numericValue = null)),
            actionAssignTagValueOnStartIds = setOf(10L),
        )
        val rule2 = assignTagRule(
            actionAssignTagValues = listOf(tag(20L, numericValue = null)),
            actionAssignTagValueOnStartIds = setOf(20L),
        )

        `when`(complexRuleInteractor.getAll()).thenReturn(listOf(rule1, rule2))

        val result = subject.processRules(
            timeStarted = 0L,
            startingTypeId = startingTypeId,
            currentTypeIds = currentTypeIds,
        )

        assertEquals(setOf(10L, 20L), result.tagIdsToSelectValueOnStart)
    }

    @Test
    fun dropsSelectOnStartWhenNumericExists(): Unit = runBlocking {
        val rule = assignTagRule(
            actionAssignTagValues = listOf(tag(30L, numericValue = 3.5)),
            actionAssignTagValueOnStartIds = setOf(30L),
        )

        `when`(complexRuleInteractor.getAll()).thenReturn(listOf(rule))

        val result = subject.processRules(
            timeStarted = 0L,
            startingTypeId = startingTypeId,
            currentTypeIds = currentTypeIds,
        )

        assertEquals(emptySet<Long>(), result.tagIdsToSelectValueOnStart)
    }

    @Test
    fun ignoresSelectOnStartWithoutMergedTag(): Unit = runBlocking {
        val rule = assignTagRule(
            actionAssignTagValues = listOf(tag(40L, numericValue = null)),
            actionAssignTagValueOnStartIds = setOf(40L, 99L),
        )

        `when`(complexRuleInteractor.getAll()).thenReturn(listOf(rule))

        val result = subject.processRules(
            timeStarted = 0L,
            startingTypeId = startingTypeId,
            currentTypeIds = currentTypeIds,
        )

        assertEquals(setOf(40L), result.tagIdsToSelectValueOnStart)
    }

    private fun assignTagRule(
        actionAssignTagValues: List<RecordBase.Tag>,
        actionAssignTagValueOnStartIds: Set<Long>,
    ): ComplexRule {
        return ComplexRule(
            id = 0L,
            disabled = false,
            action = ComplexRule.Action.AssignTag,
            actionDisallowOnlyPrevious = false,
            actionAssignTagValues = actionAssignTagValues,
            actionAssignTagValueOnStartIds = actionAssignTagValueOnStartIds,
            conditionStartingTypeIds = setOf(startingTypeId),
            conditionCurrentTypeIds = currentTypeIds,
            conditionDaysOfWeek = setOf(currentDay),
        )
    }

    private fun tag(
        tagId: Long,
        numericValue: Double?,
    ): RecordBase.Tag {
        return RecordBase.Tag(tagId = tagId, numericValue = numericValue)
    }
}

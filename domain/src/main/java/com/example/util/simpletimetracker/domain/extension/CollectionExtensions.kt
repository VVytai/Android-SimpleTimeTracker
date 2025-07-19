package com.example.util.simpletimetracker.domain.extension

import com.example.util.simpletimetracker.domain.record.model.RecordBase

/**
 * Adds item if it not in the list, otherwise removes it from the list.
 */
fun <T> MutableList<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}

fun <T> List<T>.addOrRemove(item: T): List<T> {
    return this.toMutableList().apply { addOrRemove(item) }
}

fun <T> MutableSet<T>.addOrRemove(item: T) {
    if (item in this) remove(item) else add(item)
}

fun <T> Set<T>.addOrRemove(item: T): Set<T> {
    return this.toMutableSet().apply { addOrRemove(item) }
}

fun <T, U> MutableMap<T, U>.addOrRemove(item: T, value: U) {
    if (item in this) remove(item) else put(item, value)
}

fun List<RecordBase.Tag>.addOrRemove(itemId: Long): List<RecordBase.Tag> {
    val ids = this.map { it.tagId }
    val tag = RecordBase.Tag(tagId = itemId, numericValue = null)
    return if (itemId in ids) {
        removeIf { it.tagId == itemId }
    } else {
        toMutableList().apply { add(tag) }
    }
}

operator fun <T> MutableCollection<in T>.plusAssign(element: T?) {
    if (element != null) this.add(element)
}

operator fun <T> MutableCollection<in T>.plusAssign(elements: List<T>?) {
    if (elements != null) this.addAll(elements)
}

operator fun <T> List<T>.plus(element: T?): List<T> {
    return if (element != null) this.plusElement(element) else this
}

operator fun <T> List<T>.plus(elements: List<T>?): List<T> {
    return if (elements != null) this.toMutableList().apply { addAll(elements) } else this
}

inline fun <T> List<T>.replaceWith(new: T, crossinline filter: (T) -> Boolean): List<T> {
    return this.removeIf(filter).toMutableList().apply { add(new) }
}
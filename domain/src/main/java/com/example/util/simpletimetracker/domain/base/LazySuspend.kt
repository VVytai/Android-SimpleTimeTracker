package com.example.util.simpletimetracker.domain.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

fun <T> CoroutineScope.suspendLazy(
    initializer: suspend CoroutineScope.() -> T,
) = object : SuspendLazy<T> {
    private val deferred = async(start = CoroutineStart.LAZY, block = initializer)
    override suspend operator fun invoke(): T = deferred.await()
}

interface SuspendLazy<T> {
    suspend operator fun invoke(): T
}
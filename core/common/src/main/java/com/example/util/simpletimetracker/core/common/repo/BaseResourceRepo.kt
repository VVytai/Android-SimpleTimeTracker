package com.example.util.simpletimetracker.core.common.repo

import androidx.annotation.StringRes

interface BaseResourceRepo {

    fun getString(@StringRes stringResId: Int): String
}
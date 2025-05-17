package com.example.util.simpletimetracker.core.extension

import android.text.Html.TagHandler
import android.text.Spanned
import androidx.core.text.HtmlCompat

fun String.fromHtml(tagHandler: TagHandler? = null): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler)
}

fun String.trimIfNotBlank(): String {
    return if (this.isNotBlank()) return this.trim() else this
}
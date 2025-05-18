package com.example.util.simpletimetracker.feature_views.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.text.inSpans

fun Spanned.toSpannableString(): SpannableString {
    return SpannableString(this)
}

fun String.toSpannableString(): SpannableString {
    return SpannableString(this)
}

fun SpannableString.setBackgroundSpan(
    start: Int,
    length: Int,
    @ColorInt color: Int,
): SpannableString {
    setSpan(
        BackgroundColorSpan(color),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setForegroundSpan(
    start: Int = 0,
    length: Int = this.length,
    @ColorInt color: Int,
): SpannableString {
    setSpan(
        ForegroundColorSpan(color),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setClickableSpan(
    start: Int,
    length: Int,
    onClick: () -> Unit,
): SpannableString {
    setSpan(
        createClickableSpan(onClick),
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableString.setImageSpan(
    start: Int,
    length: Int,
    drawable: Drawable,
    sizeDp: Int,
    isCentered: Boolean = false,
): SpannableString {
    val finalDrawable = drawable.apply {
        setBounds(0, 0, sizeDp.dpToPx(), sizeDp.dpToPx())
    }
    val span = if (isCentered) {
        CenteredImageSpan(finalDrawable)
    } else {
        ImageSpan(finalDrawable, DynamicDrawableSpan.ALIGN_BASELINE)
    }
    setSpan(
        span,
        start,
        start + length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    return this
}

fun SpannableStringBuilder.image(
    drawable: Drawable,
    sizeDp: Int,
    isCentered: Boolean,
    builderAction: SpannableStringBuilder.() -> Unit,
) {
    val finalDrawable = drawable.apply {
        setBounds(0, 0, sizeDp.dpToPx(), sizeDp.dpToPx())
    }
    val span = if (isCentered) {
        CenteredImageSpan(finalDrawable)
    } else {
        ImageSpan(finalDrawable, DynamicDrawableSpan.ALIGN_BASELINE)
    }
    inSpans(span, builderAction)
}

class CenteredImageSpan(
    drawable: Drawable,
) : ImageSpan(drawable) {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int {
        return drawable.bounds.width()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint,
    ) {
        canvas.save()
        val transY = top + (bottom - top) / 2 - drawable.bounds.height() / 2
        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}

private fun createClickableSpan(
    action: () -> Unit,
): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            action()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }
}
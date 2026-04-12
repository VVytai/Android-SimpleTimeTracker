package com.example.util.simpletimetracker.core.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.core.manager.KeyboardVisibilityManager
import com.example.util.simpletimetracker.domain.extension.orZero

// Can update padding or margin, which would be more appropriate.
fun View.doOnApplyWindowInsetsListener(block: View.(WindowInsetsCompat) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        // All setOnApplyWindowInsetsListener listeners should call Keyboard manager.
        KeyboardVisibilityManager.onInsetsChanged(windowInsets)
        block(windowInsets)
        windowInsets
    }
}

fun View.applyStatusBarInsets() {
    doOnApplyWindowInsetsListener { updatePadding(top = getStatusBarInsets()) }
}

fun View.applyNavBarInsets() {
    doOnApplyWindowInsetsListener { updatePadding(bottom = getNavBarInsets()) }
}

fun View.getStatusBarInsets(): Int {
    return ViewCompat.getRootWindowInsets(this)
        ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top.orZero()
}

fun View.getNavBarInsets(): Int {
    return ViewCompat.getRootWindowInsets(this)
        ?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom.orZero()
}

// Need windowSoftInputMode="adjustResize" on activity in order to work on api < 30.
fun WindowInsetsCompat.isKeyboardInsetsVisible(): Boolean {
    return isVisible(WindowInsetsCompat.Type.ime())
}

sealed interface InsetConfiguration {
    object DoNotApply : InsetConfiguration
    data class ApplyToView(val view: () -> View) : InsetConfiguration
}

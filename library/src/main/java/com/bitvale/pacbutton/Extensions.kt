package com.bitvale.pacbutton

import android.content.Context
import android.content.res.Resources
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

/**
 * Created by Alexander Kolpakov on 11/4/2018
 */
fun Context.getVectorDrawable(@DrawableRes resId: Int): VectorDrawableCompat? {
    return try {
        return VectorDrawableCompat.create(resources, resId, null)
    } catch (e: Resources.NotFoundException) {
        null
    }
}

fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}
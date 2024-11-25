
@file:Suppress("NOTHING_TO_INLINE")

package com.maya.newbulgariankeyboard.main_classes

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import kotlin.reflect.KClass


@RequiresApi(Build.VERSION_CODES.M)
@Throws(NullPointerException::class, ClassCastException::class)
fun <T : Any> Context.systemService(kClass: KClass<T>): T {
    val serviceName = this.getSystemServiceName(kClass.java)!!
    @Suppress("UNCHECKED_CAST")
    return this.getSystemService(serviceName) as T
}


@RequiresApi(Build.VERSION_CODES.M)
fun <T : Any> Context.systemServiceOrNull(kClass: KClass<T>): T? {
    return try {
        this.systemService(kClass)
    } catch (e: Exception) {
        null
    }
}


@Throws(android.content.res.Resources.NotFoundException::class)
inline fun Context.stringRes(@StringRes id: Int): String {
    return this.resources.getString(id)
}

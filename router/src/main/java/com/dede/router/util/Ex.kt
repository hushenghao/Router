package com.dede.router.util

/**
 * Created by hsh on 2019-06-05 11:15
 */

fun String?.isNull(): Boolean = this == null || this.isEmpty() || "null" == this

fun String?.notNull(): Boolean = !this.isNull()
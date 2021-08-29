package com.github.zimolab.jsarray2.core

interface IJsArrayIteratorCallback<in T, out U> {
    fun call(index: Int, value: T, total: T): U
}

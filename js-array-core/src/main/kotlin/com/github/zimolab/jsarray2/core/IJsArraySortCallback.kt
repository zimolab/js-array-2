package com.github.zimolab.jsarray2.core

interface IJsArraySortCallback<T> {
    fun compare(a: T, b: T): Int
}
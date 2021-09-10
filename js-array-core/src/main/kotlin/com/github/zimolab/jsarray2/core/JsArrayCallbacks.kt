package com.github.zimolab.jsarray2.core

interface IJsArrayIteratorCallback {
    fun call(index: Int, value: Any?, total: Any?): Any?
}

interface IJsArraySortCallback {
    fun compare(a: Any?, b: Any?): Int
}

typealias IteratorCallback2<T, R> = (index: Int, value: T)->R
typealias IteratorCallback3<T, R> = (index: Int, value: T, total: T)->R
typealias SortCallback<T> = (a: T, b: T) -> Int
package com.github.zimolab.jsarray2.core

import netscape.javascript.JSObject

interface IJsArray<T> {
    val reference: JSObject
    var length: Int
    operator fun set(index: Int, value: T?)
    operator fun get(index: Int): T?
    fun concat(other: IJsArray<T>): IJsArray<T>
    fun join(separator: String = ","): String
    fun reverse(): IJsArray<T>
    fun pop(): T?
    fun push(vararg elements: T?): Int
    fun shift(): T?
    fun unshift(vararg elements: T?): Int
    fun slice(start: Int, end: Int? = null): IJsArray<T>
    fun splice(index: Int, count: Int, vararg items: T?): IJsArray<T>
    fun fill(value: T?, start: Int = 0, end: Int? = null): IJsArray<T>
    fun find(callback: IteratorCallback2<T, Boolean>): T?
    fun findIndex(callback: IteratorCallback2<T, Boolean>): Int
    fun includes(element: T, start: Int = 0): Boolean
    fun indexOf(element: T, start: Int = 0): Int
    fun lastIndexOf(element: T, start: Int = -1): Int
    fun forLoop(callback: IteratorCallback2<T, Unit>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
    fun forEach(callback: IteratorCallback2<T, Unit>)
    fun filter(callback: IteratorCallback2<T, Boolean>): IJsArray<T>
    fun map(callback: IteratorCallback2<T, T>): IJsArray<T>
    fun every(callback: IteratorCallback2<T, Boolean>): Boolean
    fun some(callback: IteratorCallback2<T, Boolean>): Boolean
    fun reduce(callback: IteratorCallback3<T, T>): T
    fun reduceRight(callback: IteratorCallback3<T, T>): T
    fun sort(compareFunc: SortCallback<T>? = null): IJsArray<T>
    fun toJsAnyArray(): IJsArray<Any?>
}
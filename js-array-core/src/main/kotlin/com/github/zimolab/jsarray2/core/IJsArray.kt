package com.github.zimolab.jsarray2.core

interface IJsArray<T> {
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
    fun find(callback: IJsArrayIteratorCallback<T?, Boolean>): T?
    fun findIndex(callback: IJsArrayIteratorCallback<T?, Boolean>): Int
    fun includes(element: T, start: Int = 0): Boolean
    fun indexOf(element: T, start: Int = 0): Int
    fun lastIndexOf(element: T, start: Int = -1): Int
    fun forLoop(callback: IJsArrayIteratorCallback<T?, Boolean>, startIndex: Int = 0, stopIndex: Int = -1, step: Int = 1)
    fun forEach(callback: IJsArrayIteratorCallback<T?, Unit>)
    fun filter(callback: IJsArrayIteratorCallback<T?, Boolean>): IJsArray<T>
    fun map(callback: IJsArrayIteratorCallback<T?, T?>): IJsArray<T>
    fun every(callback: IJsArrayIteratorCallback<T?, Boolean>): Boolean
    fun some(callback: IJsArrayIteratorCallback<T?, Boolean>): Boolean
    fun reduce(callback: IJsArrayIteratorCallback<T?, T?>): T?
    fun reduceRight(callback: IJsArrayIteratorCallback<T?, T?>): T?
    fun sort(sortFunction: IJsArraySortCallback<T>? = null): IJsArray<T>
    fun toJsAnyArray(): IJsArray<Any?>
}
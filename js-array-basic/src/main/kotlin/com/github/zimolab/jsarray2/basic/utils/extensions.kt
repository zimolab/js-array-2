package com.github.zimolab.jsarray2.basic.utils

import netscape.javascript.JSException
import netscape.javascript.JSObject

fun JSObject.invoke(methodName: String, vararg args: Any?, silently: Boolean = true, printlnStackTrace: Boolean = true): Any? {
    return if (silently) {
        try {
            this.call(methodName, *args)
        } catch (e: JSException) {
            if (printlnStackTrace) e.printStackTrace()
            e
        }
    } else {
        this.call(methodName, args)
    }
}
package com.github.zimolab.jsarray2.ksp.processor.utils

import com.github.zimolab.jow.compiler.qualifiedName
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.subclassOf
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import netscape.javascript.JSObject

object TypeUtils {
    @ExperimentalUnsignedTypes
    val TYPES_WITH_TO_TYPED_ARRAY_FUNC by lazy {
        mutableListOf<String>(
            Boolean::class.qualifiedName!!,
            Byte::class.qualifiedName!!,
            Char::class.qualifiedName!!,
            Double::class.qualifiedName!!,
            Float::class.qualifiedName!!,
            Int::class.qualifiedName!!,
            Long::class.qualifiedName!!,
            Short::class.qualifiedName!!,
            UByte::class.qualifiedName!!,
            UInt::class.qualifiedName!!,
            ULong::class.qualifiedName!!,
            UShort::class.qualifiedName!!,
            Collection::class.qualifiedName!!,
        )
    }

    val NATIVE_TYPES = mutableListOf<String>(
        Boolean::class.qualifiedName!!,
        Int::class.qualifiedName!!,
        Double::class.qualifiedName!!,
        String::class.qualifiedName!!,
        JSObject::class.qualifiedName!!,
        // nullable类型
        Boolean::class.qualifiedName!! + "?",
        Int::class.qualifiedName!! + "?",
        Double::class.qualifiedName!! + "?",
        String::class.qualifiedName!! + "?",
        JSObject::class.qualifiedName!! + "?",
    )

    val VOID_TYPES = mutableListOf<String>(
        Unit::class.qualifiedName!!,
        Void::class.qualifiedName!!,
        Nothing::class.qualifiedName!!,
        Unit::class.qualifiedName!! + "?",
        Void::class.qualifiedName!! + "?",
        Nothing::class.qualifiedName!! + "?"
    )

    fun isNativeType(type: KSType): Boolean {
        return type.qualifiedName in NATIVE_TYPES
    }

    fun isVoidType(type: KSType): Boolean {
        return type.qualifiedName in VOID_TYPES
    }

    fun isNullable(type: KSType): Boolean {
        return type.isMarkedNullable
    }

    fun isAnyType(type: KSType): Boolean {
        return type.qualifiedName.let {
            it == Any::class.qualifiedName!! || it == Any::class.qualifiedName!! + "?"
        }
    }

    @ExperimentalUnsignedTypes
    fun hasToTypedArrayFunction(type: KSType): Boolean {
        return type.qualifiedName in TYPES_WITH_TO_TYPED_ARRAY_FUNC
    }

}
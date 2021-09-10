package com.github.zimolab.jsarray2.ksp.processor.generator.apis

import com.github.zimolab.jow.compiler.asTypeName
import com.github.zimolab.jow.compiler.of
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.ksp.processor.generator.*
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.apiOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.getterOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.jsArrayOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.paramOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.setterOf
import com.github.zimolab.jsarray2.ksp.processor.utils.TypeUtils
import com.squareup.kotlinpoet.*
import netscape.javascript.JSObject

@ExperimentalUnsignedTypes
class CommonApis(private val apis: JsArrayApis) {
    private val arrayType = apis.jsArrayType

    val set by lazy {
        val invoke = "${IJsArray<*>::reference.name}.setSlot"
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        apiOf(
            "set",
            Unit::class.asTypeName(),
            arrayOf(KModifier.OVERRIDE, KModifier.OPERATOR),
            paramOf("index", Int::class.asTypeName()),
            paramOf("value", arrayType.asTypeName()),
        ) {
            """
            $invoke(index, $mapping(value))
            """
        }
    }

    val get by lazy {
        apiOf(
            "get",
            arrayType.asTypeName(),
            arrayOf(KModifier.OVERRIDE, KModifier.OPERATOR),
            paramOf("index", Int::class.asTypeName()),
        ) {
            val invoke = apis.eval.name
            val mapping = if (apis.requireMapping) apis.mappingReturnValue.name else ""
            val type = if (apis.requireMapping) JSObject::class.asTypeName() else arrayType.asTypeName()
            """
            "{let __tmp__ = this[index]; __tmp__ == undefined? null : __tmp__;}".let{
                val ret = $invoke(it)
                if(ret is %T)
                    return $mapping(ret)
                else
                    throw RuntimeException("return value type is not as expected")
            }
            """.of(type)
        }
    }

    val length by lazy {
        PropertySpec.builder(LENGTH, Int::class.asTypeName())
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true)
            .setter(setterOf(Int::class.asTypeName()) {
                val invoke = apis.eval.name
                """
                "this.$LENGTH = ${"$"}value".let{
                    $invoke(it)
                }
                """
            })
            .getter(getterOf {
                val invoke = apis.eval.name
                """
                "this.$LENGTH".let{
                    val ret = $invoke(it)
                    if(ret is Int)
                        return ret
                    else
                        throw RuntimeException("return value type is not as expected")
                }
                """
            })
            .build()
    }

    val getAny by lazy {
        val invoke = apis.eval.name
        apiOf(
            "getAny",
            Any::class.asTypeName(true),
            arrayOf(KModifier.OPEN),
            paramOf("index", Int::class.asTypeName()),
        ) {
            """
            "{let __tmp__ = this[index]; __tmp__ == undefined? null : __tmp__;}".let{
                return $invoke(it)
            }
            """
        }
    }

    val concat by lazy {
        val invoke = apis.invoke.name
        val newInstance = apis.newInstance.name
        val arg = "other.${IJsArray<*>::reference.name}"
        apiOf(
            CONCAT,
            jsArrayOf(arrayType.asTypeName()),
            Triple("other", jsArrayOf(arrayType.asTypeName()), false)
        ) {
            """
            val ret = $invoke("$CONCAT", $arg)
            if(ret is %T)
                return $newInstance(ret)
            else
                throw RuntimeException("return value type is not as expected")
            """.of(JSObject::class)
        }
    }

    val join by lazy {
        val invoke = apis.eval.name
        apiOf(JOIN, String::class.asTypeName(), Triple("separator", String::class.asTypeName(), false)) {
            """
            "{let __tmp__ = this.$JOIN('${"$"}separator');__tmp__ == undefined? null : __tmp__;}".let {
                val ret = $invoke(it)
                if(ret is String)
                    return ret
                else
                    throw RuntimeException("return value type is not as expected")
            }
            """
        }
    }

    val reverse by lazy {
        val invoke = apis.eval.name
        val reference = "this.${IJsArray<*>::reference.name}"
        apiOf(REVERSE, jsArrayOf(arrayType.asTypeName())) {
            """
             "{let __tmp__ = this.$REVERSE();__tmp__ == undefined? null : __tmp__;}".let {
                val ret = $invoke(it)
                if(ret == $reference)
                    return this
                else
                    throw RuntimeException("return value type is not as expected")
             }
            """
        }
    }

    val pop by lazy {
        val invoke = apis.eval.name
        val mapping = if (apis.requireMapping) apis.mappingReturnValue.name else ""
        val type = if (apis.requireMapping) JSObject::class.asTypeName() else arrayType.asTypeName()

        apiOf(POP, arrayType.asTypeName()) {
            """
            "{let __tmp__ = this.$POP();__tmp__ == undefined? null : __tmp__;}".let {
                val ret = $invoke(it)
                return if(ret is %T)
                    $mapping(ret)
                else
                    throw RuntimeException("return value type is not as expected")
             }
            """.of(type)
        }
    }

    val popAny by lazy {
        val invoke = apis.eval.name
        apiOf("${POP}Any", Any::class.asTypeName(true), arrayOf(KModifier.OPEN)) {
            """
            "{let __tmp__ = this.$POP();__tmp__ == undefined? null : __tmp__;}".let{
                return $invoke(it)
            }
            """
        }
    }

    val push by lazy {
        val invoke = apis.invoke.name
        val mapping = apis.mappingArgument.name
        val elements =
            if (apis.requireMapping)
                JsArrayApis.vararg("elements", arrayType.asTypeName(), mapping)
            else
                JsArrayApis.vararg("elements", arrayType.asTypeName(), null)

        apiOf(PUSH, Int::class.asTypeName(), paramOf("elements", arrayType.asTypeName(), true)) {
            """
            val ret = $invoke("$PUSH", $elements)
            if(ret is Int)
               return ret
            else
                throw RuntimeException("return value type is not as expected")
            """
        }
    }

    val shift by lazy {
        val invoke = apis.eval.name
        val mapping = if (apis.requireMapping) apis.mappingReturnValue.name else ""
        val type = if (apis.requireMapping) JSObject::class.asTypeName() else arrayType.asTypeName()
        apiOf(SHIFT, arrayType.asTypeName()) {
            """
            "{let __tmp__ = this.$SHIFT();__tmp__ == undefined? null : __tmp__;}".let {
                val ret = $invoke(it)
                return if(ret is %T)
                    $mapping(ret)
                else
                    throw RuntimeException("return value type is not as expected")
             }
            """.of(type)
        }
    }

    val shiftAny by lazy {
        val invoke = apis.eval.name
        apiOf("${SHIFT}Any", Any::class.asTypeName(true), arrayOf(KModifier.OPEN)) {
            """
            "{let __tmp__ = this.$SHIFT();__tmp__ == undefined? null : __tmp__;}".let{
                return $invoke(it)
            }
            """
        }
    }

    val unshift by lazy {
        val invoke = apis.invoke.name
        val mapping = apis.mappingArgument.name
        val elements =
            if (apis.requireMapping)
                JsArrayApis.vararg("elements", arrayType.asTypeName(), mapping)
            else
                JsArrayApis.vararg("elements", arrayType.asTypeName(), null)

        apiOf(UNSHIFT, Int::class.asTypeName(), paramOf("elements", arrayType.asTypeName(), true)) {
            """
            val ret = $invoke("$UNSHIFT", $elements)
            if(ret is Int)
               return ret
            else
                throw RuntimeException("return value type is not as expected")
            """
        }
    }

    val slice by lazy {
        val invoke = apis.invoke.name
        val newInstance = apis.newInstance.name
        apiOf(
            SLICE,
            jsArrayOf(arrayType.asTypeName()),
            paramOf("start", Int::class.asTypeName()),
            paramOf("end", Int::class.asTypeName(true))
        ) {
            """
            val ret = if(end == null) {
                $invoke("$SLICE", start)
            } else {
                $invoke("$SLICE", start, end)
            }
            if(ret is %T)
                return $newInstance(ret)
            else
                throw RuntimeException("return value type is not as expected")                
            """.of(JSObject::class)
        }
    }

    val splice by lazy {
        val invoke = apis.invoke.name
        val newInstance = apis.newInstance.name
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        val items = JsArrayApis.vararg("items", arrayType.asTypeName(), mapping)
        apiOf(
            SPLICE,
            jsArrayOf(arrayType.asTypeName()),
            paramOf("index", Int::class.asTypeName()),
            paramOf("count", Int::class.asTypeName()),
            paramOf("items", arrayType.asTypeName(), true)
        ) {
            """
            val ret = $invoke("$SPLICE", index, count, $items)
            if(ret is %T)
                return $newInstance(ret)
            else
                 throw RuntimeException("return value type is not as expected")
            """.of(JSObject::class)
        }

    }

    val fill by lazy {
        val invoke = apis.invoke.name
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        val reference = "this.${IJsArray<*>::reference.name}"
        apiOf(
            FILL,
            jsArrayOf(arrayType.asTypeName()),
            paramOf("value", arrayType.asTypeName()),
            paramOf("start", Int::class.asTypeName()),
            paramOf("end", Int::class.asTypeName(true))
        ) {
            """
            val ret = if(end==null) {
                $invoke("$FILL", $mapping(value), start)
            } else {
                $invoke("$FILL", $mapping(value), start, end)
            }
            if(ret == $reference)
                return this
            else
                throw RuntimeException("return value type is not as expected")
            """
        }
    }

    val includes by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        apiOf(
            INCLUDES,
            Boolean::class.asTypeName(),
            paramOf("element", arrayType.asTypeName()),
            paramOf("start", Int::class.asTypeName()),
        ) {
            var code = if (!TypeUtils.isNullable(arrayType))
                ""
            else
                """
                if(element == null) {
                    "this.$INCLUDES(null, ${'$'}start) || this.$INCLUDES(undefined, ${'$'}start)".let{
                        val ret = $eval(it)
                        if(ret is %T)
                            return ret
                        else
                            throw RuntimeException("return value type is not as expected")
                    }
                }
                """.of(Boolean::class)
            code +=
                """
                val ret = $invoke("$INCLUDES", $mapping(element), start)
                if(ret is %T)
                    return ret
                else
                    throw RuntimeException("return value type is not as expected")
                """.of(Boolean::class)
            code
        }
    }

    val includesAny by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = ""
        apiOf(
            "${INCLUDES}Any",
            Boolean::class.asTypeName(),
            arrayOf(KModifier.OPEN),
            paramOf("element", Any::class.asTypeName(true)),
            paramOf("start", Int::class.asTypeName()),
        ) {
            """
            if(element == null) {
                "this.$INCLUDES(null, ${'$'}start) || this.$INCLUDES(undefined, ${'$'}start)".let{
                    val ret = $eval(it)
                    if(ret is %T)
                        return ret
                    else
                        throw RuntimeException("return value type is not as expected")
                }
            }                    
            val ret = $invoke("$INCLUDES", $mapping(element), start)
            if(ret is %T)
                return ret
            else
                throw RuntimeException("return value type is not as expected")
            """.of(Boolean::class, Boolean::class)
        }
    }

    val indexOf by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        apiOf(
            INDEX_OF,
            Int::class.asTypeName(),
            paramOf("element", arrayType.asTypeName()),
            paramOf("start", Int::class.asTypeName()),
        ) {
            var code = if (!TypeUtils.isNullable(arrayType))
                ""
            else
                """
                if(element == null) {
                    "{let __tmp__ = this.$INDEX_OF(null, ${'$'}start); __tmp__ == -1? this.$INDEX_OF(undefined, ${'$'}start) : __tmp__;}".let{
                        val ret = $eval(it)
                        if(ret is %T)
                            return ret
                        else
                            throw RuntimeException("return value type is not as expected")
                    }
                }
                """.of(Int::class)
            code +=
                """
                val ret = $invoke("$INDEX_OF", $mapping(element), start)
                if(ret is %T)
                    return ret
                else
                    throw RuntimeException("return value type is not as expected")
                """.of(Int::class)
            code
        }
    }

    val indexOfAny by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = ""
        apiOf(
            "${INDEX_OF}Any",
            Int::class.asTypeName(),
            arrayOf(KModifier.OPEN),
            paramOf("element", Any::class.asTypeName(true)),
            paramOf("start", Int::class.asTypeName()),
        ) {
            """
            if(element == null) {
                "{let __tmp__ = this.$INDEX_OF(null, ${'$'}start); __tmp__ == -1? this.$INDEX_OF(undefined, ${'$'}start) : __tmp__;}".let{
                    val ret = $eval(it)
                    if(ret is %T)
                        return ret
                    else
                        throw RuntimeException("return value type is not as expected")
                }
            }
            val ret = $invoke("$INDEX_OF", $mapping(element), start)
            if(ret is %T)
                return ret
            else
               throw RuntimeException("return value type is not as expected")            
            """.of(Int::class, Int::class)
        }
    }

    val lastIndexOf by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = if (apis.requireMapping) apis.mappingArgument.name else ""
        apiOf(
            LAST_INDEX_OF,
            Int::class.asTypeName(),
            paramOf("element", arrayType.asTypeName()),
            paramOf("start", Int::class.asTypeName()),
        ) {
            var code = if (!TypeUtils.isNullable(arrayType))
                ""
            else
                """
                if(element == null) {
                    "{let __tmp__ = this.$LAST_INDEX_OF(null, ${'$'}start); __tmp__ == -1? this.$LAST_INDEX_OF(undefined, ${'$'}start) : __tmp__;}".let{
                        val ret = $eval(it)
                        if(ret is %T)
                            return ret
                        else
                            throw RuntimeException("return value type is not as expected")
                    }
                }
                """.of(Int::class)
            code +=
                """
                val ret = $invoke("$LAST_INDEX_OF", $mapping(element), start)
                if(ret is %T)
                    return ret
                else
                    throw RuntimeException("return value type is not as expected")
                """.of(Int::class)
            code
        }
    }

    val lastIndexOfAny by lazy {
        val invoke = apis.invoke.name
        val eval = apis.eval.name
        val mapping = ""
        apiOf(
            "${LAST_INDEX_OF}Any",
            Int::class.asTypeName(),
            arrayOf(KModifier.OPEN),
            paramOf("element", Any::class.asTypeName(true)),
            paramOf("start", Int::class.asTypeName()),
        ) {
            """
            if(element == null) {
                "{let __tmp__ = this.$LAST_INDEX_OF(null, ${'$'}start); __tmp__ == -1? this.$LAST_INDEX_OF(undefined, ${'$'}start) : __tmp__;}".let{
                    val ret = $eval(it)
                    if(ret is %T)
                        return ret
                    else
                        throw RuntimeException("return value type is not as expected")
                }
            }
            val ret = $invoke("$LAST_INDEX_OF", $mapping(element), start)
            if(ret is %T)
                return ret
            else
               throw RuntimeException("return value type is not as expected")            
            """.of(Int::class, Int::class)
        }
    }
}
package com.github.zimolab.jsarray2.ksp.processor.generator.apis

import com.github.zimolab.jow.compiler.asTypeName
import com.github.zimolab.jow.compiler.of
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.ksp.processor.generator.*
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.apiOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.funOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.getterOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.jsArrayOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.paramOf
import com.github.zimolab.jsarray2.ksp.processor.generator.apis.JsArrayApis.Companion.setterOf
import com.github.zimolab.jsarray2.ksp.processor.utils.TypeUtils
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject

@ExperimentalUnsignedTypes
class CommonApis(private val apis: JsArrayApis) {
    private val arrayType = apis.jsArrayType

    val set by lazy {
        FunSpec.builder("set")
            .addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
            .addParameter("index", Int::class)
            .addParameter("value", arrayType.asTypeName())
            .addCode(JsArrayApis.format("reference.setSlot(index, ${apis.arg("value", arrayType)})"))
            .build()
    }

    val get by lazy {
        FunSpec.builder("get")
            .addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
            .addParameter("index", Int::class)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = ${apis.eval.name}(\"{let __tmp = this[\$index];${JsArrayApis.u2n("__tmp")}}\")\n" +
                            apis.ret("ret", arrayType.asTypeName())
                )
            )
            .returns(arrayType.asTypeName())
            .build()
    }

    val length by lazy {
        PropertySpec.builder(LENGTH, Int::class.asTypeName())
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true)
            .setter(setterOf(Int::class.asTypeName()){
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
                        throw RuntimeException("return value is not as expected")
                }
                """
            })
            .build()
    }

    val getAny by lazy {
        FunSpec.builder("getAny")
            .addModifiers(KModifier.OPEN)
            .addParameter("index", Int::class)
            .addCode(
                JsArrayApis.format(
                    "return  ${apis.eval.name}(\"{let __tmp = this[\$index];${JsArrayApis.u2n("__tmp")}}\")\n",
                )
            )
            .returns(Any::class.asTypeName().copy(nullable = true))
            .build()
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
                throw RuntimeException("return value is not as expected")
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
        funOf("${POP}Any", Any::class.asTypeName(true)) {
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
        funOf("${SHIFT}Any", Any::class.asTypeName(true)) {
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
        apiOf(SLICE, jsArrayOf(arrayType.asTypeName()), paramOf("start", Int::class.asTypeName()), paramOf("end", Int::class.asTypeName(true))) {
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
        FunSpec.builder(SPLICE)
            .addParameter("index", Int::class)
            .addParameter("count", Int::class)
            .addParameter("items", arrayType.asTypeName(), KModifier.VARARG)
            .addModifiers(KModifier.OVERRIDE)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = ${apis.invoke.name}(\"$SPLICE\", index, count, ${
                                apis.arg(
                                    "items",
                                    arrayType,
                                    isVararg = true
                                )
                            })\n" +
                            apis.retNewArray("ret")
                )
            )
            .returns(IJsArray::class.asTypeName().parameterizedBy(arrayType.asTypeName()))
            .build()
    }

    val fill by lazy {
        FunSpec.builder(FILL)
            .addParameter("value", arrayType.asTypeName())
            .addParameter("start", Int::class)
            .addParameter("end", Int::class.asTypeName().copy(nullable = true))
            .addModifiers(KModifier.OVERRIDE)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = if(end == null){\n" +
                            "   ${apis.invoke.name}(\"$FILL\", ${apis.arg("value", arrayType)}, start)\n" +
                            "} else {\n" +
                            "   ${apis.invoke.name}(\"$FILL\", ${apis.arg("value", arrayType)}, start, end)\n" +
                            "}\n" +
                            apis.retNewArray("ret")
                )
            )
            .returns(IJsArray::class.asTypeName().parameterizedBy(arrayType.asTypeName()))
            .build()
    }

    val includes by lazy {
        FunSpec.builder(INCLUDES)
            .addParameter("element", arrayType.asTypeName())
            .addParameter("start", Int::class)
            .addModifiers(KModifier.OVERRIDE).apply {
                val code = if (TypeUtils.isNullable(arrayType)) {
                    JsArrayApis.format(
                        "" +
                                "val ret = if(element == null){\n" +
                                "   ${apis.eval.name}(\"this.$INCLUDES(null, \$start) || this.$INCLUDES(undefined, \$start)\")\n" +
                                "} else {\n" +
                                "   ${apis.invoke.name}(\"$INCLUDES\", ${apis.arg("element", arrayType)}, start)\n" +
                                "}\n" +
                                apis.ret("ret", Boolean::class.asTypeName())
                    )
                } else {
                    JsArrayApis.format(
                        "" +
                                "val ret = ${apis.invoke.name}(\"$INCLUDES\", ${
                                    apis.arg(
                                        "element",
                                        arrayType
                                    )
                                }, start)\n" +
                                apis.ret("ret", Boolean::class.asTypeName())
                    )
                }
                addCode(code)
            }
            .returns(Boolean::class)
            .build()
    }

    val includesAny by lazy {
        FunSpec.builder("includesAny")
            .addModifiers(KModifier.OPEN)
            .addParameter("element", Any::class.asTypeName().copy(nullable = true))
            .addParameter("start", Int::class)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = if(element == null){\n" +
                            "   ${apis.eval.name}(\"this.$INCLUDES(null, \$start) || this.$INCLUDES(undefined, \$start)\")\n" +
                            "} else {\n" +
                            "   ${apis.invoke.name}(\"$INCLUDES\", element, start)\n" +
                            "}\n" +
                            apis.ret("ret", Boolean::class.asTypeName())
                )
            )
            .returns(Boolean::class)
            .build()
    }

    val indexOf by lazy {
        FunSpec.builder(INDEX_OF)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("element", arrayType.asTypeName())
            .addParameter("start", Int::class).apply {
                val code = if (TypeUtils.isNullable(arrayType)) {
                    JsArrayApis.format(
                        "" +
                                "val ret = if(element == null){\n" +
                                "   ${apis.eval.name}(\n\"{let __tmp=this.$INDEX_OF(null, \$start);__tmp!=-1?__tmp:this.$INDEX_OF(undefined, \$start);}\"\n)\n" +
                                "} else {\n" +
                                "   ${apis.invoke.name}(\"$INDEX_OF\", ${apis.arg("element", arrayType)}, start)\n" +
                                "}\n" +
                                apis.ret("ret", Int::class.asTypeName())
                    )
                } else {
                    JsArrayApis.format(
                        "" +
                                "val ret = ${apis.invoke.name}(\"$INDEX_OF\", ${
                                    apis.arg(
                                        "element",
                                        arrayType
                                    )
                                }, start)\n" +
                                apis.ret("ret", Int::class.asTypeName())
                    )
                }
                addCode(code)
            }
            .returns(Int::class)
            .build()
    }

    val indexOfAny by lazy {
        FunSpec.builder("indexOfAny")
            .addModifiers(KModifier.OPEN)
            .addParameter("element", Any::class.asTypeName().copy(nullable = true))
            .addParameter("start", Int::class)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = ${apis.invoke.name}(\"$INDEX_OF\", element, start)\n" +
                            apis.ret("ret", Int::class.asTypeName())
                )
            )
            .returns(Int::class)
            .build()
    }

    val lastIndexOf by lazy {
        FunSpec.builder(LAST_INDEX_OF)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("element", arrayType.asTypeName())
            .addParameter("start", Int::class).apply {
                val code = if (TypeUtils.isNullable(arrayType)) {
                    JsArrayApis.format(
                        "" +
                                "val ret = if(element == null){\n" +
                                "   ${apis.eval.name}(\n\"{let __tmp=this.$LAST_INDEX_OF(null, \$start);__tmp!=-1?__tmp:this.$LAST_INDEX_OF(undefined, \$start);}\"\n)\n" +
                                "} else {\n" +
                                "   ${apis.invoke.name}(\"$LAST_INDEX_OF\", ${
                                    apis.arg(
                                        "element",
                                        arrayType
                                    )
                                }, start)\n" +
                                "}\n" +
                                apis.ret("ret", Int::class.asTypeName())
                    )
                } else {
                    JsArrayApis.format(
                        "" +
                                "val ret = ${apis.invoke.name}(\"$LAST_INDEX_OF\", ${
                                    apis.arg(
                                        "element",
                                        arrayType
                                    )
                                }, start)\n" +
                                apis.ret("ret", Int::class.asTypeName())
                    )
                }
                addCode(code)
            }
            .returns(Int::class)
            .build()
    }

    val lastIndexOfAny by lazy {
        FunSpec.builder("lastIndexOfAny")
            .addModifiers(KModifier.OPEN)
            .addParameter("element", Any::class.asTypeName().copy(nullable = true))
            .addParameter("start", Int::class)
            .addCode(
                JsArrayApis.format(
                    "" +
                            "val ret = ${apis.invoke.name}(\"$LAST_INDEX_OF\", element, start)\n" +
                            apis.ret("ret", Int::class.asTypeName())
                )
            )
            .returns(Int::class)
            .build()
    }


}
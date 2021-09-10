package com.github.zimolab.jsarray2.ksp.processor.generator.apis

import com.github.zimolab.jow.compiler.asTypeName
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.core.IJsArrayIteratorCallback
import com.github.zimolab.jsarray2.core.IJsArraySortCallback
import com.github.zimolab.jsarray2.ksp.processor.generator.*
import com.github.zimolab.jsarray2.ksp.processor.utils.TypeUtils
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject

@ExperimentalUnsignedTypes
class IterationApis(private val apis: JsArrayApis) {
    private val jsArrayType = apis.jsArrayType

    private fun jsArrayTypenameOf(jsArrayType: TypeName): TypeName {
        return IJsArray::class.asTypeName().parameterizedBy(jsArrayType)
    }

    private fun callback2ParamOf(paramName: String, inputType: TypeName, outputType: TypeName): ParameterSpec {
        return ParameterSpec.builder(
            paramName,
            LambdaTypeName.get(
                receiver = null,
                ParameterSpec("index", Int::class.asTypeName()),
                ParameterSpec("value", inputType),
                returnType = outputType
            )
        ).build()
    }

    private fun javaCodeOfCallback2(anyVer: Boolean, jsCallbackName: String, jsCode: String): String {
        val mapping = apis.mappingReturnValue.name
        val inject = inject.name
        val uninject = uninject.name
        val eval = apis.eval.name
        return """
               val cb = object : %T {
                override fun call(index: Int, value: Any?, total: Any?): Any? {
        ${
            if (anyVer)
                "return callback(index, value)"
            else if (apis.requireMapping)
                "return callback(index, $mapping(value))"
            else
                CodeBlock.of(
                    """
                if(value is %T)
                    return callback(index, value)
                else
                    throw RuntimeException("value type at current index is not as expected")""",
                    jsArrayType.asTypeName()
                ).toString()
        }
                }
              }
              $inject("$jsCallbackName", cb)
              val ret = $eval("$jsCode")
              $uninject("$jsCallbackName")"""
    }

    private fun jsCodeOfCallback2(jsApi: String, jsCallbackName: String): String {
        return "" +
                "{" +
                "let __tmp = this.$jsApi((item, index, arr)=>{return this.$jsCallbackName.call(index, item==undefined?null:item)});" +
                "__tmp==undefined?null:__tmp;" +
                "}"
    }

    private fun functionOfCallback2(
        jsApi: String,
        anyVer: Boolean,
        funReturnType: TypeName,
        callbackReturnType: TypeName,
        returnJsArrayInstance: Boolean = false
    ): FunSpec {
        val funName = if (anyVer) {
            "${jsApi}Any"
        } else {
            jsApi
        }

        val modifier = if (anyVer) {
            KModifier.OPEN
        } else {
            KModifier.OVERRIDE
        }

        val callbackParam = if (anyVer) {
            callback2ParamOf("callback", Any::class.asTypeName().copy(nullable = true), callbackReturnType)
        } else {
            callback2ParamOf("callback", jsArrayType.asTypeName(), callbackReturnType)
        }

        return FunSpec.builder(funName)
            .addModifiers(modifier)
            .addParameter(callbackParam)
            .returns(funReturnType).apply {
                val mapping = apis.mappingReturnValue.name
                val jsCallbackName = "__${funName}__cb__"
                val jsCode = jsCodeOfCallback2(jsApi, jsCallbackName)
                var javaCode = javaCodeOfCallback2(anyVer, jsCallbackName, jsCode)

                if (!returnJsArrayInstance) {
                    javaCode += """
                    ${
                        if (TypeUtils.isVoidType(funReturnType))
                            "return Unit"
                        else if (TypeUtils.isAnyType(funReturnType))
                            "return ret"
                        else if (apis.requireMapping && funReturnType == jsArrayType.asTypeName())
                            "return $mapping(ret)"
                        else
                            CodeBlock.of(
                                """
                            if(ret is %T)
                                 return ret
                               else
                                 throw RuntimeException("return type is not as expected")
                        """, funReturnType
                            ).toString()
                    }"""
                } else {
                    val reference = JsArrayApis.PROP_REF
                    val newInstance = apis.newInstance.name
                    javaCode += CodeBlock.of(
                        """
                        return if(ret is %T) {
                            if(ret == this.$reference)
                                this
                            else
                                $newInstance(ret)
                        } else {
                            throw RuntimeException("return value type is not as expected")
                        }
                    """, JSObject::class
                    )
                }
                addCode(JsArrayApis.format(javaCode, IJsArrayIteratorCallback::class.asTypeName()))
            }
            .build()
    }

    private fun callback3ParamOf(paramName: String, inputType: TypeName, outputType: TypeName): ParameterSpec {
        return ParameterSpec.builder(
            paramName,
            LambdaTypeName.get(
                receiver = null,
                ParameterSpec("index", Int::class.asTypeName()),
                ParameterSpec("value", inputType),
                ParameterSpec("total", inputType),
                returnType = outputType
            )
        ).build()
    }

    private fun javaCodeOfCallback3(anyVer: Boolean, jsCallbackName: String, jsCode: String): String {
        val mapping = apis.mappingReturnValue.name
        val inject = inject.name
        val uninject = uninject.name
        val eval = apis.eval.name
        return """
               val cb = object : %T {
                override fun call(index: Int, value: Any?, total: Any?): Any? {
        ${
            if (anyVer)
                "return callback(index, value, total)"
            else if (apis.requireMapping)
                "return callback(index, $mapping(value), $mapping(total))"
            else
                CodeBlock.of(
                    """
                if(value is %T && total is %T)
                    return callback(index, value, total)
                else
                    throw RuntimeException("value type at current index is not as expected")""",
                    jsArrayType.asTypeName(), jsArrayType.asTypeName()
                ).toString()
        }
                }
              }
              $inject("$jsCallbackName", cb)
              val ret = $eval("$jsCode")
              $uninject("$jsCallbackName")"""
    }

    private fun jsCodeOfCallback3(jsApi: String, jsCallbackName: String): String {
        return "" +
                "{" +
                "let __tmp = this.$jsApi((total, item, index, arr)=>{return this.$jsCallbackName.call(index, item==undefined?null:item, total==undefined?null:total)});" +
                "__tmp==undefined?null:__tmp;" +
                "}"
    }

    private fun functionOfCallback3(
        jsApi: String,
        anyVer: Boolean,
        funReturnType: TypeName,
        callbackReturnType: TypeName,
        returnJsArrayInstance: Boolean = false
    ): FunSpec {
        val funName = if (anyVer) {
            "${jsApi}Any"
        } else {
            jsApi
        }

        val modifier = if (anyVer) {
            KModifier.OPEN
        } else {
            KModifier.OVERRIDE
        }

        val callbackParam = if (anyVer) {
            callback3ParamOf("callback", Any::class.asTypeName().copy(nullable = true), callbackReturnType)
        } else {
            callback3ParamOf("callback", jsArrayType.asTypeName(), callbackReturnType)
        }

        return FunSpec.builder(funName)
            .addModifiers(modifier)
            .addParameter(callbackParam)
            .returns(funReturnType).apply {
                val mapping = apis.mappingReturnValue.name
                val jsCallbackName = "__${funName}__cb__"
                val jsCode = jsCodeOfCallback3(jsApi, jsCallbackName)
                var javaCode = javaCodeOfCallback3(anyVer, jsCallbackName, jsCode)

                if (!returnJsArrayInstance) {
                    javaCode += """
                    ${
                        if (TypeUtils.isVoidType(funReturnType))
                            "return Unit"
                        else if (TypeUtils.isAnyType(funReturnType))
                            "return ret"
                        else if (apis.requireMapping && funReturnType == jsArrayType.asTypeName())
                            "return $mapping(ret)"
                        else
                            CodeBlock.of(
                                """
                            if(ret is %T)
                                 return ret
                               else
                                 throw RuntimeException("return type is not as expected")
                        """, funReturnType
                            ).toString()
                    }"""
                } else {
                    val reference = JsArrayApis.PROP_REF
                    val newInstance = apis.newInstance.name
                    javaCode += CodeBlock.of(
                        """
                        return if(ret is %T) {
                            if(ret == this.$reference)
                                this
                            else
                                $newInstance(ret)
                        } else {
                            throw RuntimeException("return value type is not as expected")
                        }
                    """, JSObject::class
                    )
                }
                addCode(JsArrayApis.format(javaCode, IJsArrayIteratorCallback::class.asTypeName()))
            }
            .build()
    }

    private fun sortCallbackParamOf(paramName: String, eleType: TypeName): ParameterSpec {
        return ParameterSpec.builder(
            paramName,
            LambdaTypeName.get(
                receiver = null,
                ParameterSpec("a", eleType),
                ParameterSpec("b", eleType),
                returnType = Int::class.asTypeName()
            ).copy(nullable = true)
        ).build()
    }

    private fun javaCodeOfSortFunc(anyVer: Boolean, jsCallbackName: String, jsCode: String): String {
        val mapping = apis.mappingReturnValue.name
        val inject = inject.name
        val uninject = uninject.name
        val eval = apis.eval.name
        val invoke = apis.invoke.name
        val reference = IJsArray<*>::reference.name
        val newInstance = apis.newInstance.name
        return CodeBlock.of(
            """
                if(compareFunc == null) {
                    val ret = $invoke("$SORT")
            ${CodeBlock.of(
            """
                    return if(ret is %T) {
                        if(ret == this.$reference)
                            this
                        else
                            $newInstance(ret)
                    } else {
                        throw RuntimeException("return value type is not as expected")
                    }
              """, JSObject::class)
            }
                } 
               val cb = object : %T {
                override fun compare(a: Any?, b: Any?): Int {
        ${
                if (anyVer)
                    "return compareFunc(a, b)"
                else if (apis.requireMapping)
                    "return compareFunc($mapping(a), $mapping(b))"
                else
                    CodeBlock.of(
                        """
                if(a is %T && b is %T)
                    return compareFunc(a, b)
                else
                    throw RuntimeException("value type at current index is not as expected")""",
                        jsArrayType.asTypeName(), jsArrayType.asTypeName()
                    ).toString()
            }
                }
              }
              $inject("$jsCallbackName", cb)
              val ret = $eval("$jsCode")
              $uninject("$jsCallbackName")
              ${
                CodeBlock.of(
                    """
              return if(ret is %T) {
                if(ret == this.$reference)
                    this
                else
                    $newInstance(ret)
              } else {
                throw RuntimeException("return value type is not as expected")
              }
              """, JSObject::class
                )
            }
              """, IJsArraySortCallback::class.asTypeName()
        ).toString()
    }

    private fun jsCodeOfSortCallback(jsApi: String, jsCallbackName: String): String {
        return "" +
                "{" +
                "let __tmp = this.$jsApi((a, b)=>{return this.$jsCallbackName.compare(a==undefined?null:a, b==undefined?null:b)});" +
                "__tmp==undefined?null:__tmp;" +
                "}"
    }

    private fun functionOfSort(
        jsApi: String,
        anyVer: Boolean
    ): FunSpec {
        val funName = if (anyVer) {
            "${jsApi}Any"
        } else {
            jsApi
        }

        val modifier = if (anyVer) {
            KModifier.OPEN
        } else {
            KModifier.OVERRIDE
        }

        val callbackParam = if (anyVer) {
            sortCallbackParamOf("compareFunc", Any::class.asTypeName().copy(nullable = true))
        } else {
            sortCallbackParamOf("compareFunc", jsArrayType.asTypeName())
        }

        return FunSpec.builder(funName)
            .addModifiers(modifier)
            .addParameter(callbackParam)
            .returns(jsArrayTypenameOf(jsArrayType.asTypeName())).apply {
                val jsCallbackName = "__${funName}__cb__"
                val jsCode = jsCodeOfSortCallback(jsApi, jsCallbackName)
                val javaCode = javaCodeOfSortFunc(anyVer, jsCallbackName, jsCode)
                addCode(JsArrayApis.format(javaCode))
            }
            .build()
    }

    val inject by lazy {
        FunSpec.builder("inject")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("name", String::class)
            .addParameter("javaObject", Any::class)
            .addCode(JsArrayApis.format("${JsArrayApis.PROP_REF}.setMember(name, javaObject)"))
            .build()
    }

    val uninject by lazy {
        FunSpec.builder("uninject")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("name", String::class)
            .addCode(JsArrayApis.format("${JsArrayApis.PROP_REF}.removeMember(name)"))
            .build()
    }

    val find by lazy {
        functionOfCallback2(FIND, false, jsArrayType.asTypeName(), Boolean::class.asTypeName())
    }

    val findAny by lazy {
        functionOfCallback2(FIND, true, Any::class.asTypeName().copy(nullable = true), Boolean::class.asTypeName())
    }

    val findIndex by lazy {
        functionOfCallback2(FIND_INDEX, false, Int::class.asTypeName(), Boolean::class.asTypeName())
    }

    val findIndexAny by lazy {
        functionOfCallback2(FIND_INDEX, true, Int::class.asTypeName(), Boolean::class.asTypeName())
    }

    val forEach by lazy {
        functionOfCallback2(FOR_EACH, false, Unit::class.asTypeName(), Unit::class.asTypeName())
    }

    val forEachAny by lazy {
        functionOfCallback2(FOR_EACH, true, Unit::class.asTypeName(), Unit::class.asTypeName())
    }

    val filter by lazy {
        functionOfCallback2(
            FILTER,
            false,
            jsArrayTypenameOf(jsArrayType.asTypeName()),
            Boolean::class.asTypeName(),
            true
        )
    }

    val filterAny by lazy {
        functionOfCallback2(
            FILTER,
            true,
            jsArrayTypenameOf(jsArrayType.asTypeName()),
            Boolean::class.asTypeName(),
            true
        )
    }

    val some by lazy {
        functionOfCallback2(SOME, false, Boolean::class.asTypeName(), Boolean::class.asTypeName())
    }

    val someAny by lazy {
        functionOfCallback2(SOME, true, Boolean::class.asTypeName(), Boolean::class.asTypeName())
    }

    val every by lazy {
        functionOfCallback2(EVERY, false, Boolean::class.asTypeName(), Boolean::class.asTypeName())
    }

    val everyAny by lazy {
        functionOfCallback2(EVERY, true, Boolean::class.asTypeName(), Boolean::class.asTypeName())
    }

    val map by lazy {
        functionOfCallback2(
            com.github.zimolab.jsarray2.ksp.processor.generator.MAP, false,
            jsArrayTypenameOf(jsArrayType.asTypeName()), jsArrayType.asTypeName(), true
        )
    }

    val mapAny by lazy {
        functionOfCallback2(
            com.github.zimolab.jsarray2.ksp.processor.generator.MAP, true,
            jsArrayTypenameOf(jsArrayType.asTypeName()), jsArrayType.asTypeName(), true
        )
    }

    val reduceRight by lazy {
        functionOfCallback3(REDUCE_RIGHT, false, jsArrayType.asTypeName(), jsArrayType.asTypeName())
    }

    val reduceRightAny by lazy {
        functionOfCallback3(REDUCE_RIGHT, true, jsArrayType.asTypeName(), jsArrayType.asTypeName())
    }

    val reduceRightAny2 by lazy {
        functionOfCallback3(
            REDUCE_RIGHT,
            true,
            Any::class.asTypeName().copy(nullable = true),
            Any::class.asTypeName().copy(nullable = true)
        )
    }

    val reduce by lazy {
        functionOfCallback3(REDUCE, false, jsArrayType.asTypeName(), jsArrayType.asTypeName())
    }

    val reduceAny by lazy {
        functionOfCallback3(REDUCE, true, jsArrayType.asTypeName(), jsArrayType.asTypeName())
    }

    val reduceAny2 by lazy {
        functionOfCallback3(
            REDUCE,
            true,
            Any::class.asTypeName().copy(nullable = true),
            Any::class.asTypeName().copy(nullable = true)
        )
    }

    val sort by lazy {
        functionOfSort(SORT, false)
    }


    val nonAnyVerApis by lazy {
        arrayOf(
            inject,
            uninject,
            find,
            findIndex,
            filter,
            some,
            every,
            map
        )
    }

    val anyVerApis by lazy {
        arrayOf(
            findAny,
            findIndexAny,
            filterAny,
            someAny,
            everyAny,
            mapAny
        )
    }

}
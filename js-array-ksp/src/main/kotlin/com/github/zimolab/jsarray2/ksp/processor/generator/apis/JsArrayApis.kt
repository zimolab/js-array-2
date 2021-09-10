package com.github.zimolab.jsarray2.ksp.processor.generator.apis

import com.github.zimolab.jow.compiler.asTypeName
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.ksp.processor.generator.*
import com.github.zimolab.jsarray2.ksp.processor.utils.TypeUtils
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import netscape.javascript.JSObject
import org.kotlin.formatter.KotlinFormatter

@Suppress("SameParameterValue")
@ExperimentalUnsignedTypes
class JsArrayApis(val jsArrayType: KSType) {
    val requireMapping = !(TypeUtils.isAnyType(jsArrayType) || TypeUtils.isNativeType(jsArrayType))

    companion object {
        private val CodeFormatter = KotlinFormatter(maxLineLength = 128)

        fun format(code: String): String {
            return CodeFormatter.format(code).replace(" ", "·")
        }

        fun format(code: String, vararg args: Any?): String {
            return CodeFormatter.format(CodeBlock.of(code, *args).toString()).replace(" ", "·")
        }

        fun apiOf(apiName: String, returnType: TypeName, vararg params: Triple<String, TypeName, Boolean>, code: ()->String): FunSpec {
            return FunSpec.builder(apiName)
                .addModifiers(KModifier.OVERRIDE).apply {
                    params.forEach { (name, type, isVararg)->
                        if (isVararg)
                            addParameter(name, type, KModifier.VARARG)
                        else
                            addParameter(name, type)
                    }
                }
                .apply {
                    addCode(format(code().trim()))
                }
                .returns(returnType)
                .build()
        }

        fun apiOf(
            apiName: String,
            returnType: TypeName,
            modifies: Array<KModifier> = arrayOf(KModifier.OVERRIDE),
            vararg params: Triple<String, TypeName, Boolean>,
            code: () -> String
        ): FunSpec {
            return FunSpec.builder(apiName)
                .addModifiers(*modifies).apply {
                    params.forEach { (name, type, isVararg)->
                        if (isVararg)
                            addParameter(name, type, KModifier.VARARG)
                        else
                            addParameter(name, type)
                    }
                }
                .apply {
                    addCode(format(code().trim()))
                }
                .returns(returnType)
                .build()
        }

        fun setterOf(paramType: TypeName, code: ()->String): FunSpec {
            return FunSpec.setterBuilder()
                .addParameter("value", paramType).apply {
                    addCode(format(code().trim()))
                }
                .build()
        }

        fun getterOf(code: () -> String): FunSpec {
            return FunSpec.getterBuilder()
                .addCode(format(code().trim()))
                .build()
        }

        fun jsArrayOf(typeName: TypeName): TypeName {
            return IJsArray::class.asTypeName().parameterizedBy(typeName)
        }

        fun u2n(name: String) = "$name==undefined?null:$name"

        fun asArg(argName: String, argType: KSType, mappingFunc: String? = null, isVararg: Boolean = false): String {
            if (isVararg) {
                return if (mappingFunc == null) {
                    if (TypeUtils.hasToTypedArrayFunction(argType)) {
                        "*($argName.toTypedArray())"
                    } else {
                        "*$argName"
                    }
                } else {
                    "*($argName.map{ $mappingFunc(it) }.toTypedArray())"
                }
            } else {
                return if (mappingFunc == null) {
                    argName
                } else {
                    "$mappingFunc($argName)"
                }
            }
        }

        fun vararg(argName: String, argType: TypeName, mappingFunc: String?): String {
            return if (mappingFunc == null) {
                if (TypeUtils.hasToTypedArrayFunction(argType)) {
                    "*($argName.toTypedArray())"
                } else {
                    "*$argName"
                }
            } else {
                "*($argName.map{ $mappingFunc(it) }.toTypedArray())"
            }
        }

        val PROP_REF = IJsArray<*>::reference.name

        fun paramOf(name: String, type: TypeName, isVararg: Boolean = false) = Triple(name, type, isVararg)

    }

    fun arg(argName: String, argType: KSType, isVararg: Boolean = false): String {
        val mappingFunc = if (requireMapping && argType == jsArrayType) {
            mappingArgument.name
        } else {
            null
        }
        return asArg(argName, argType, mappingFunc, isVararg)
    }

    fun ret(retName: String, retType: TypeName): String {
        val mappingFunc = if (requireMapping && retType == jsArrayType.asTypeName()) {
            mappingReturnValue.name
        } else {
            null
        }
        return if (mappingFunc == null) {
            CodeBlock.of(
                "" +
                        "if($retName is %T)\n" +
                        "   return $retName\n" +
                        "throw RuntimeException(\"return value type is not as expected\")",
                retType
            ).toString()
        } else {
            "return $mappingFunc($retName)"
        }
    }

    fun retNewArray(retName: String): String {
        return "if($retName is JSObject){\n" +
                "   if($retName == this.$PROP_REF)\n" +
                "       return this\n" +
                "   return ${newInstance.name}($retName)" +
                "}\n" +
                "throw RuntimeException(\"cannot create an array from return value\")"
    }

    val invoke by lazy {
        FunSpec.builder("invoke")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("method", String::class)
            .addParameter("args", Any::class.asTypeName().copy(nullable = true), KModifier.VARARG)
            .addCode("return $PROP_REF.call(method, *args)")
            .returns(Any::class.asTypeName().copy(nullable = true))
            .build()
    }

    val eval by lazy {
        FunSpec.builder("eval")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("script", String::class)
            .addCode("return $PROP_REF.eval(script)")
            .returns(Any::class.asTypeName().copy(nullable = true))
            .build()
    }

    fun call(methodName: String, vararg args: String): String {
        return if (args.isEmpty())
            "${eval.name}(\"{let __tmp=this.$methodName();${u2n("__tmp")}}\")"
        else {
            val argList = args.joinToString(",") { "'\$$it'" }
            "${eval.name}(\"{let __tmp=this.$methodName($argList);${u2n("__tmp")}}\")"
        }
    }

    val mappingArgument by lazy {
        FunSpec.builder("mappingArgument")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("originArgument", jsArrayType.asTypeName())
            .returns(Any::class.asTypeName().copy(nullable = true))
            .build()
    }

    val mappingReturnValue by lazy {
        FunSpec.builder("mappingReturnValue")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("originReturn", Any::class.asTypeName().copy(nullable = true))
            .returns(jsArrayType.asTypeName())
            .build()
    }

    val newInstance by lazy {
        FunSpec.builder("newInstance")
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("originValue", JSObject::class)
            .returns(IJsArray::class.asTypeName().parameterizedBy(jsArrayType.asTypeName()))
            .build()
    }

    val primaryConstructor by lazy {
        FunSpec.constructorBuilder()
            .addParameter(PROP_REF, JSObject::class)
            .build()
    }

    val commonApis by lazy {
        CommonApis(this)
    }

    val iterationApis by lazy {
        IterationApis(this)
    }
}
package com.github.zimolab.jsarray2.ksp.processor.resolve

import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.packageNameStr
import com.github.zimolab.jow.compiler.qualifiedNameStr
import com.github.zimolab.jow.compiler.simpleNameStr
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.ksp.annotation.JsArrayClass
import com.github.zimolab.jsarray2.ksp.processor.AnnotationProcessingError
import com.github.zimolab.jsarray2.ksp.processor.utils.Logger
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.*

class ClassResolver(
    private val declaration: KSClassDeclaration,
    private val annotation: KSAnnotation?,
    private val options: Map<String, String>
) {
    companion object {
        const val OPT_KEY_OUTPUT_CLASS_PREFIX = "output_class_prefix"
        const val OPT_KEY_OUTPUT_CLASS_SUFFIX = "output_class_suffix"
        const val OPT_VAL_OUTPUT_CLASS_PREFIX = "Abs"
        const val OPT_VAL_OUTPUT_CLASS_SUFFIX = ""
    }

    fun resolvePackageName(): String {
        return declaration.packageNameStr
    }

    fun resolveClassName(): String {
        return declaration.simpleNameStr
    }

    fun resolveQualifiedName(): String {
        return declaration.qualifiedNameStr
    }

    fun resolveClassType(): KSType {
        return declaration.asType(emptyList())
    }

    fun resolveContainingFile(): KSFile? {
        return declaration.containingFile
    }

    fun resolveClassDoc(): String {
        return resolveAnnotationArgument(JsArrayClass::classDoc.name, "")
    }

    fun resolveOutputClassName(): String {
        return resolveAnnotationArgument(JsArrayClass::outputClassName.name, "").ifEmpty {
            if (OPT_KEY_OUTPUT_CLASS_PREFIX in options || OPT_KEY_OUTPUT_CLASS_SUFFIX in options) {

                val prefix = options[OPT_KEY_OUTPUT_CLASS_PREFIX]?.let {
                    it.ifEmpty { OPT_VAL_OUTPUT_CLASS_PREFIX }
                }?: OPT_VAL_OUTPUT_CLASS_PREFIX

                val suffix = options[OPT_KEY_OUTPUT_CLASS_SUFFIX]?.let {
                    it.ifEmpty { OPT_VAL_OUTPUT_CLASS_SUFFIX }
                }?: OPT_VAL_OUTPUT_CLASS_SUFFIX

                "$prefix${resolveClassName()}$suffix"

            } else {
                "$OPT_VAL_OUTPUT_CLASS_PREFIX${resolveClassName()}$OPT_VAL_OUTPUT_CLASS_SUFFIX"
            }
        }
    }

    fun resolveOutputFilename(): String {
       val filename = resolveOutputClassName()
        return resolveAnnotationArgument(JsArrayClass::outputFilename.name, filename).ifEmpty {
            filename
        }
    }

    fun resolveOutputFileEncoding(): String {
        return resolveAnnotationArgument(
            JsArrayClass::outputFileEncoding.name,
            JsArrayClass.DEFAULT_OUTPUT_ENCODING
        ).ifEmpty { JsArrayClass.DEFAULT_OUTPUT_ENCODING }
    }

    private inline fun <reified T> resolveAnnotationArgument(argumentName: String, defaultValue: T): T {
        return if (annotation == null)
            defaultValue
        else
            annotation.findArgument(argumentName, defaultValue)
    }

    fun resolveTypeParameters(): List<KSTypeParameter> {
        return declaration.typeParameters
    }

    fun resolveJsArrayType(): KSType {
        val superClass = declaration.getAllSuperTypes()
            .filter { it.declaration.qualifiedNameStr == IJsArray::class.qualifiedName }.first()
        if (superClass.arguments.isEmpty()) {
            AnnotationProcessingError("必须为父类明确泛型类型").let {
                Logger.error(it)
                throw it
            }
        }
        val type = superClass.arguments[0].type?.resolve() ?: AnnotationProcessingError("类型不能为空").let {
                Logger.error(it)
                throw it
            }
        return type
    }

    fun resolveSuperTypes(): Sequence<KSTypeReference> {
        return declaration.superTypes
    }
}
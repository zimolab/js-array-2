package com.github.zimolab.jsarray2.ksp.processor.resolve

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

class ResolvedClass(declaration: KSClassDeclaration, annotation: KSAnnotation, options: Map<String, String> = mapOf()) {
    private val resolver = ClassResolver(declaration, annotation, options)

    val packageName by lazy {
        resolver.resolvePackageName()
    }

    val simpleName by lazy {
        resolver.resolveClassName()
    }

    val qualifiedName by lazy {
        resolver.resolveQualifiedName()
    }

    val type by lazy {
        resolver.resolveClassType()
    }

    val typeParameters by lazy {
        resolver.resolveTypeParameters()
    }

    val containingFile by lazy {
        resolver.resolveContainingFile()
    }

    val classDoc by lazy {
        resolver.resolveClassDoc()
    }

    val superTypes by lazy {
        resolver.resolveSuperTypes()
    }

    val meta by lazy {
        MetaData()
    }

    inner class MetaData {
        val outputClassName by lazy {
            resolver.resolveOutputClassName()
        }

        val outputFilename by lazy {
            resolver.resolveOutputFilename()
        }

        val outputFileEncoding by lazy {
            resolver.resolveOutputFileEncoding()
        }

        val jsArrayType: KSType by lazy {
            resolver.resolveJsArrayType()
        }
    }

}
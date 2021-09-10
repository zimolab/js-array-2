package com.github.zimolab.jsarray2.ksp.processor

import com.github.zimolab.jow.compiler.*
import com.github.zimolab.jow.compiler.qualifiedName
import com.github.zimolab.jow.compiler.simpleName
import com.github.zimolab.jsarray2.core.IJsArray
import com.github.zimolab.jsarray2.ksp.annotation.JsArrayClass
import com.github.zimolab.jsarray2.ksp.processor.generator.JsArrayClassGenerator
import com.github.zimolab.jsarray2.ksp.processor.resolve.ResolvedClass
import com.github.zimolab.jsarray2.ksp.processor.utils.Logger
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import kotlin.math.log

class JsArrayProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {
    companion object {
        const val OPT_KEY_ENABLE_LOGGING = "enable_logging"
        const val OPT_VAL_ENABLE_LOGGING = "true"
    }

    private val classGenerator = JsArrayClassGenerator(environment.options, environment.codeGenerator)

    init {
        Logger.init(environment.options)
        val enableLogging = (environment.options[OPT_KEY_ENABLE_LOGGING]?: OPT_VAL_ENABLE_LOGGING).equals("true", true)
        if (enableLogging) {
            Logger.enable()
        } else {
            Logger.disable()
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        Logger.debug("开始处理注解")
        val symbols = resolver.getSymbolsWithAnnotation(JsArrayClass::class.qualifiedName!!)
        val notProcessedSymbols = symbols.filter { !it.validate() }.toList()
        Logger.debug("找到${symbols.count()}个被注解的符号")
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(SymbolVisitor(), Unit)
            }
        Logger.shutdown()
        return notProcessedSymbols
    }

    inner class SymbolVisitor: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            super.visitClassDeclaration(classDeclaration, data)
            Logger.debug("正在处理${classDeclaration}")
            if (!classDeclaration.subclassOf(IJsArray::class)) {
                AnnotationProcessingError("被注解的接口必须继承自${IJsArray::class}接口").let {
                    Logger.error(it)
                    throw it
                }
            }
            val resolvedClass = processAnnotated(classDeclaration)
            classGenerator.submit(resolvedClass)
        }

        private fun processAnnotated(declaration: KSClassDeclaration): ResolvedClass {
            val annotation = declaration.findAnnotations(JsArrayClass::class).firstOrNull()
            if (annotation == null) {
                val e = AnnotationProcessingError("无法找到${JsArrayClass::class.simpleName}注解")
                Logger.error(e)
                throw e
            }
            return ResolvedClass(declaration, annotation, environment.options)
        }
    }
}
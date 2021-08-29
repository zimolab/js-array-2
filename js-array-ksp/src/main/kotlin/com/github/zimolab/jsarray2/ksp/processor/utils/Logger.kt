package com.github.zimolab.jsarray2.ksp.processor.utils

import com.github.zimolab.jow.compiler.debug
import com.github.zimolab.jow.compiler.error
import com.github.zimolab.jow.compiler.shutdown
import com.github.zimolab.jsarray2.ksp.processor.JsArrayProcessor
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.logging.FileHandler
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.io.path.Path

object Logger {
    private val DEFAULT_LOG_FILE: Path = Path(System.getProperty("user.home"), ".js-array2", "ksp").toAbsolutePath()
    private lateinit var logger: Logger
    private var enabled: Boolean = false

    fun init(options: Map<String, String>) {
        // 创建日志文件
        val logFile = if (options.containsKey("log_file")) {
            "${options["log_file"]}-${LocalDateTime.now().toString().replace(":", ".")}.log"
        } else {
            "$DEFAULT_LOG_FILE-${LocalDateTime.now().toString().replace(":", ".")}.log"
        }
        val tmp = File(logFile).parentFile
        if (tmp != null && !tmp.exists()) {
            tmp.mkdirs()
        }
        LogManager.getLogManager().readConfiguration(javaClass.getResourceAsStream("/log.properties"))
        logger = Logger.getLogger(JsArrayProcessor::class.qualifiedName)
        logger.addHandler(FileHandler(logFile))
    }

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }

    fun debug(msg: String) {
        if (enabled)
            logger.debug(msg)
    }

    fun info(msg: String) {
        if (enabled)
            logger.info(msg)
    }

    fun warn(msg: String) {
        if (enabled)
            logger.warning(msg)
    }

    fun error(msg: String) {
        if (enabled)
            logger.error(msg)
    }

    fun error(e: Throwable, throws: Boolean = false) {
        if (enabled)
            logger.error(e, throws)
    }

    fun shutdown() {
        logger.shutdown()
    }
}
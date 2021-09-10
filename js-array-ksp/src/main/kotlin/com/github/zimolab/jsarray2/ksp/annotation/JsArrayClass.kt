package com.github.zimolab.jsarray2.ksp.annotation

@Target(AnnotationTarget.CLASS)
annotation class JsArrayClass(
    val outputClassName: String = "",
    val outputFilename: String = "",
    val outputFileEncoding: String = DEFAULT_OUTPUT_ENCODING,
    val classDoc: String = ""
) {
    companion object {
        const val DEFAULT_OUTPUT_ENCODING = "UTF-8"
    }
}

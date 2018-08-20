package com.zengyuhao.demo.androidaudiovideodev.appcore

import java.io.Closeable

private inline fun <T : Closeable?> Array<T>.use(block: ()->Unit) {
    var exception: Throwable? = null
    try {
        return block()
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            exception == null -> forEach { it?.close() }
            else -> forEach {
                try {
                    it?.close()
                } catch (closeException: Throwable) {
                    exception.addSuppressed(closeException)
                }
            }
        }
    }
}
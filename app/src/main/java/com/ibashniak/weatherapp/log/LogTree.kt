package com.ibashniak.weatherapp.log

import timber.log.Timber

internal class LogTree(private val logLevel: Int) : Timber.DebugTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= logLevel
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return "(${element.fileName}:${element.lineNumber})_${element.methodName}"
    }
}

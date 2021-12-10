package com.ibashniak.weatherapp.log

import timber.log.Timber

internal open class LogTree(private val logLevel: Int) : Timber.DebugTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= logLevel
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format(
            "%s:%d %s",
            element.methodName,
            element.lineNumber,
            super.createStackElementTag(element)
        )
    }
}

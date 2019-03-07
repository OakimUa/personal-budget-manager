package org.oakim.pbm.util

object LOG {
    private val severityFieldSize: Int by lazy { LogSeverity.values().map { it.name.length }.max() ?: 1 }
    private fun message(severity: LogSeverity, message: String) {
        println("[${severity.name.padStart(severityFieldSize, ' ')}] $message")
    }

    fun debug(message: String) = LOG.message(LogSeverity.DEBUG, message)
    fun info(message: String) = LOG.message(LogSeverity.INFO, message)
    fun warn(message: String) = LOG.message(LogSeverity.WARN, message)
    fun error(message: String) = LOG.message(LogSeverity.ERROR, message)

    enum class LogSeverity {
        DEBUG, INFO, WARN, ERROR
    }
}
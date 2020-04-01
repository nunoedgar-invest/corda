package net.corda.common.logging.errorReporting

import java.util.*

object ErrorReporting {
    private var errorReporter: ErrorReporter? = null

    fun initialise(localeString: String, location: String) {
        errorReporter = ErrorReporterImpl(location, Locale.forLanguageTag(localeString), CordaErrorContextProvider())
    }

    internal fun getReporter() : ErrorReporter {
        return errorReporter ?: throw ReportingUninitializedException()
    }
}
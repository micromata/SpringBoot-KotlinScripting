package de.micromata.kotlinscripting.utils

import mu.KotlinLogging
import org.jetbrains.kotlin.ir.types.IdSignatureValues.result
import kotlin.script.experimental.api.*

private val log = KotlinLogging.logger {}

/**
 * Utility class for handling script results.
 * Logs the script execution result and reports.
 * Formats the script lines with error markers.
 * @param result The result of the script execution.
 * @param script The script that was executed.
 * @param logSeverity The severity level for logging.
 * @return The result of the script execution.
 */
internal object KotlinScriptUtils {
    fun handleResult(
        result: ResultWithDiagnostics<EvaluationResult>?,
        script: String,
        logSeverity: ScriptDiagnostic.Severity = ScriptDiagnostic.Severity.INFO
    ): Any? {
        if (result == null) {
            log.error { "No result (timeout?)" }
            return null
        }
        val scriptLines = script.lines()
        result.reports.forEach { report ->
            val severity = report.severity
            if (severity < logSeverity) {
                return@forEach
            }
            val message = report.message
            val location = report.location
            var line1 = "[$severity] $message"
            var line2: String? = null
            location?.let {
                // line1 = "[$severity] $message: ${it.start.line}:${it.start.col} to ${it.end?.line}:${it.end?.col}"
                line1 = "[$severity] $message: line ${it.start.line} to ${it.end?.line}"
                val lineIndex = it.start.line - 1 // Zeilenindex anpassen
                if (lineIndex in scriptLines.indices) {
                    val line = scriptLines[lineIndex]
                    val startCol = it.start.col - 1
                    val endCol = (it.end?.col ?: line.length) - 1

                    // Teile die Zeile auf und füge die Marker ein
                    val markedLine = buildString {
                        append(">")
                        append(line.substring(0, startCol))
                        append(">>>")  // Markierung für Startposition
                        append(line.substring(startCol, endCol))
                        append("<<<")  // Markierung für Endposition
                        append(line.substring(endCol))
                    }
                    line2 = markedLine
                }
            }
            log(severity, line1)
            line2?.let { log(severity, it) }
        }
        val returnValue = extractResult(result)
        if (result is ResultWithDiagnostics.Success) {
            println("Script result with success: ${returnValue}")
        } else {
            println("*** Script result: ${result.valueOrNull()}")
            result.reports.forEach {
                println("Script report: ${it.message}")
            }
        }
        return returnValue
    }

    internal fun loadScript(filename: String): String {
        return object {}.javaClass.classLoader.getResource(filename)?.readText()
            ?: throw IllegalArgumentException("Script not found: $filename")
    }

    private fun extractResult(result: ResultWithDiagnostics<EvaluationResult>): Any? {
        val returnValue = result.valueOrNull()?.returnValue
        return if (returnValue is ResultValue.Value) {
            returnValue.value
        } else {
            returnValue
        }
    }

    private fun log(serverity: ScriptDiagnostic.Severity, message: String) {
        when (serverity) {
            ScriptDiagnostic.Severity.FATAL -> log.error { message }
            ScriptDiagnostic.Severity.ERROR -> log.error { message }
            ScriptDiagnostic.Severity.WARNING -> log.warn { message }
            ScriptDiagnostic.Severity.INFO -> log.info { message }
            ScriptDiagnostic.Severity.DEBUG -> log.debug { message }
        }
    }
}

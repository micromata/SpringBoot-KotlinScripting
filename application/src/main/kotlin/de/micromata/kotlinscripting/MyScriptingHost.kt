package de.micromata.kotlinscripting

import mu.KotlinLogging
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

private val log = KotlinLogging.logger {}

class CustomScriptingHost(
    private val customClassLoader: ClassLoader
) : BasicJvmScriptingHost() {

    override fun eval(
        script: SourceCode,
        compilationConfiguration: ScriptCompilationConfiguration,
        evaluationConfiguration: ScriptEvaluationConfiguration?
    ): ResultWithDiagnostics<EvaluationResult> {
        val originalClassLoader = Thread.currentThread().contextClassLoader
        return try {
            // Setzen des gewünschten ClassLoaders
            Thread.currentThread().contextClassLoader = customClassLoader
            log.info { "CustomScriptingHost: Setting custom ClassLoader: $customClassLoader" }
            super.eval(script, compilationConfiguration, evaluationConfiguration)
        } finally {
            // Zurücksetzen des ursprünglichen ClassLoaders
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }
}

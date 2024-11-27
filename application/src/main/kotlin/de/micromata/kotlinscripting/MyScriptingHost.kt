package de.micromata.kotlinscripting

import de.micromata.kotlinscripting.business.ThreadLocalStorage
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
            ThreadLocalStorage.threadLocal.set(Constants.THREADLOCAL_TEST)
            // Trying to set the custom ClassLoader here.
            // Thread.currentThread().contextClassLoader = customClassLoader
            // log.info { "CustomScriptingHost: Setting custom ClassLoader: $customClassLoader" }
            super.eval(script, compilationConfiguration, evaluationConfiguration)
        } finally {
            // Resetting the original ClassLoader:
            // Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }
}

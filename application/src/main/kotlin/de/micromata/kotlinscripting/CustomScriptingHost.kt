package de.micromata.kotlinscripting

import de.micromata.kotlinscripting.business.ThreadLocalStorage
import mu.KotlinLogging
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

private val log = KotlinLogging.logger {}

/**
 * If you want to pass a threadlocal variable, you can use this class.
 * Otherwise, BasicJvmScriptingHost() can be used directly.
 */
class CustomScriptingHost(
    private val customClassLoader: ClassLoader? = null
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
            if (customClassLoader != null) {
                Thread.currentThread().contextClassLoader = customClassLoader
                log.info { "CustomScriptingHost: Setting custom ClassLoader: $customClassLoader" }
            }
            super.eval(script, compilationConfiguration, evaluationConfiguration)
        } finally {
            // Resetting the original ClassLoader:
            if (customClassLoader != null) {
                Thread.currentThread().contextClassLoader = originalClassLoader
            }
        }
    }
}

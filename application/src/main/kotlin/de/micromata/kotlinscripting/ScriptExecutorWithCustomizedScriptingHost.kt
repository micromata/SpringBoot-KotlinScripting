package de.micromata.kotlinscripting

import de.micromata.kotlinscripting.utils.KotlinScriptUtils
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

private val log = KotlinLogging.logger {}

class ScriptExecutorWithCustomizedScriptingHost {
    private var evalException: Exception? = null

    fun executeScript(): Any? {
        //val classLoader = CustomClassLoader(Thread.currentThread().contextClassLoader)
        val script = KotlinScriptUtils.loadScript("useContextAndThreadLocal.kts")
        val scriptingHost = CustomScriptingHost() // (classLoader)
        val compilationConfig = ScriptCompilationConfiguration {
            jvm {
                // dependenciesFromClassloader(classLoader = classLoader, wholeClasspath = true)
                dependenciesFromClassloader(wholeClasspath = true)
            }
            providedProperties("context" to KotlinScriptContext::class)
            compilerOptions.append("-nowarn")
        }
        val context = KotlinScriptContext()
        context.setProperty("testVariable", Constants.TEST_VAR)
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            /*jvm {
                baseClassLoader(classLoader) // Without effect. ClassLoader will be overwritten by the UrlClassLoader.
            }*/
            providedProperties("context" to context)
        }
        val scriptSource = script.toScriptSource()
        val executor = Executors.newSingleThreadExecutor()
        var future: Future<ResultWithDiagnostics<EvaluationResult>>? = null
        try {
            future = executor.submit<ResultWithDiagnostics<EvaluationResult>> {
                scriptingHost.eval(scriptSource, compilationConfig, evaluationConfiguration)
            }
            val result = future.get(10, TimeUnit.SECONDS)  // Timeout
            return KotlinScriptUtils.handleResult(result, script)
        } catch (ex: TimeoutException) {
            log.info("Script execution was cancelled due to timeout.")
            future?.cancel(true)  // Attempt to cancel
            evalException = ex
            log.error("scripting.error.timeout")
        } catch (ex: Exception) {
            log.info("Exception on Kotlin script execution: ${ex.message}", ex)
            evalException = ex
            log.error("Exception on Kotlin script execution: ${ex.message}")
        } finally {
            executor.shutdownNow()
        }
        return null
    }
}

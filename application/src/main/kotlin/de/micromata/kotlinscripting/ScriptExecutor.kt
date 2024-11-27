package de.micromata.kotlinscripting

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm

private val log = KotlinLogging.logger {}

class ScriptExecutor {
    private var evalException: Exception? = null

    fun executeScript(): ResultWithDiagnostics<EvaluationResult>? {
        val classLoader = CustomClassLoader(Thread.currentThread().contextClassLoader)

        val scriptingHost = CustomScriptingHost(classLoader)
        val compilationConfig = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromClassloader(classLoader = classLoader, wholeClasspath = true)
                //dependencies(JvmDependency(dependencies))
                //dependenciesFromClassloader(classLoader = classLoader, wholeClasspath = true)
            }
            providedProperties("context" to KotlinScriptContext::class)
            compilerOptions.append("-nowarn")
        }
        val context = KotlinScriptContext()
        context.setProperty("testVariable", Constants.TEST_VAR)
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            jvm {
                baseClassLoader(classLoader)
            }
            providedProperties("context" to context)
        }
        val scriptSource = script.toScriptSource()
        val executor = Executors.newSingleThreadExecutor()
        var future: Future<ResultWithDiagnostics<EvaluationResult>>? = null
        try {
            future = executor.submit<ResultWithDiagnostics<EvaluationResult>> {
                scriptingHost.eval(scriptSource, compilationConfig, evaluationConfiguration)
            }
            return future.get(10, TimeUnit.SECONDS)  // Timeout
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

    companion object {
        val simplestScript = "\"Hello world!\""
        val script = """
            import de.micromata.kotlinscripting.business.ThreadLocalStorage
            val sb = StringBuilder()
            sb.appendLine("Hello world!")
            val threadLocalVal = ThreadLocalStorage.threadLocal.get()
            if (threadLocalVal == "${Constants.THREADLOCAL_TEST}") {
                sb.appendLine("ThreadLocal: ${'$'}threadLocalVal (OK)")
            } else {
                sb.appendLine("ThreadLocal: ${'$'}threadLocalVal (*** ERROR, ${Constants.THREADLOCAL_TEST} expected)")
            }
            val testVar = context.getProperty("testVariable")
            if (testVar == "${Constants.TEST_VAR}") {
                sb.appendLine("Context: ${'$'}testVar (OK)")
            } else {
                sb.appendLine("Context: ${'$'}testVar (*** ERROR, ${Constants.TEST_VAR} expected)")
            }
            var loader = Thread.currentThread().contextClassLoader
            val classLoaders = mutableListOf<ClassLoader>()
            while (loader != null) {
                classLoaders.add(loader)
                loader = loader.parent
            }
            sb.append("ClassLoader: ")
            sb.appendLine("${'$'}{classLoaders.joinToString(", parent: ") { it.toString() }}")
            sb.appendLine("Classpath: ${'$'}{System.getProperty("java.class.path")}")
            //sb.appendLine(de.micromata.springbootkotlinscripting.ScriptExecutor.script)
            sb.toString()
        """.trimIndent()
    }
}

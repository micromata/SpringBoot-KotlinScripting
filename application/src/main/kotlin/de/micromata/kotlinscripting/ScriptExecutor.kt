package de.micromata.kotlinscripting

import mu.KotlinLogging
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependencyFromClassLoader
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

private val log = KotlinLogging.logger {}

class ScriptExecutor {
    private var evalException: Exception? = null

    fun executeScript(): Any? {
        val classLoader = CustomClassLoader(Thread.currentThread().contextClassLoader)

        //val scriptingHost = BasicJvmScriptingHost()
        val scriptingHost = CustomScriptingHost(classLoader)
        val dependencies =
            listOf(File("/Users/kai/workspace/Micromata/SpringBoot-KotlinScripting/build/libs/SpringBoot-KotlinScripting-0.0.1-SNAPSHOT.jar"))
        val compilationConfig = ScriptCompilationConfiguration {
            jvm {
                dependencies(JvmDependencyFromClassLoader { classLoader })
                //dependencies(JvmDependency(dependencies))
                //dependenciesFromClassloader(classLoader = classLoader, wholeClasspath = true)
            }
        }
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            jvm {
                //actualClassLoader(CustomClassLoader(Thread.currentThread().contextClassLoader))
                baseClassLoader(classLoader)
            }
        }
        val scriptSource = script.toScriptSource()
        val executor = Executors.newSingleThreadExecutor()
        var future: Future<ResultWithDiagnostics<EvaluationResult>>? = null
        try {
            future = executor.submit<ResultWithDiagnostics<EvaluationResult>> {
                scriptingHost.eval(scriptSource, compilationConfig, evaluationConfiguration)
            }
            return future.get(500, TimeUnit.SECONDS)  // Timeout
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
        val script = """
            val sb = StringBuilder()
            sb.appendln("Hello world!")
            var loader = Thread.currentThread().contextClassLoader
            while (loader != null) {
                sb.appendln("ClassLoader: ${'$'}loader")
                loader = loader.parent
            }
            sb.appendln("Classpath: ${'$'}{System.getProperty("java.class.path")}")
            //sb.appendln(de.micromata.springbootkotlinscripting.ScriptExecutor.script)
            sb.toString()
        """.trimIndent()
    }
}

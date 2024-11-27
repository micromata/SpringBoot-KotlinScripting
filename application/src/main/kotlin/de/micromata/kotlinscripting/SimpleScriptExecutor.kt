package de.micromata.kotlinscripting

import de.micromata.kotlinscripting.utils.KotlinScriptUtils
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class SimpleScriptExecutor {
    fun executeScript(scriptFile: String): Any? {
        val script = KotlinScriptUtils.loadScript(scriptFile)
        val scriptingHost = BasicJvmScriptingHost()
        val compilationConfig = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromClassloader(wholeClasspath = true)
            }
            providedProperties("context" to KotlinScriptContext::class)
            compilerOptions.append("-nowarn")
        }
        val context = KotlinScriptContext()
        context.setProperty("testVariable", Constants.TEST_VAR)
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            providedProperties("context" to context)
        }
        val scriptSource = script.toScriptSource()
        val result = scriptingHost.eval(scriptSource, compilationConfig, evaluationConfiguration)
        return KotlinScriptUtils.handleResult(result, script)
    }
}

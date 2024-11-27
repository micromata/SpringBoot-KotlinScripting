package de.micromata.kotlinscripting

import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.valueOrNull


@SpringBootApplication
class DemoApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DemoApplication>(*args)

            val scriptExecutor = ScriptExecutor()
            val result = scriptExecutor.executeScript()
            if (result is ResultWithDiagnostics.Success) {
                val returnValue = result.valueOrNull()?.returnValue
                val output = if (returnValue is ResultValue.Value) {
                    returnValue.value
                }   else {
                    returnValue
                }
                println("Script result with success: ${output}")
            } else if (result is ResultWithDiagnostics<*>) {
                println("*** Script result: ${result.valueOrNull()}")
                result.reports.forEach {
                    println("Script report: ${it.message}")
                }
            } else {
                println("*** Script result: $result")
            }
            System.exit(0) // Not an elegant way, but here it is enough.

        }
    }
}

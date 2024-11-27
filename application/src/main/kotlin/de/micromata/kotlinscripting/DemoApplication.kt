package de.micromata.kotlinscripting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DemoApplication>(*args)

            val scriptExecutor = ScriptExecutor()
            val result = scriptExecutor.executeScript()
            println("Script result: $result")
        }
    }
}

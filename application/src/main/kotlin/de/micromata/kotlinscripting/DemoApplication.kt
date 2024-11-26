package de.micromata.kotlinscripting

import Foo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)

    val scriptExecutor = ScriptExecutor()
    val result = scriptExecutor.executeScript()
    println("Script result: $result")
    Foo().bar()
}

package de.micromata.kotlinscripting

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val log = KotlinLogging.logger {}

@SpringBootApplication
class DemoApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DemoApplication>(*args)
            runScript("Hello world") { SimpleScriptExecutor().executeScript("helloWorld.kts") }
            runScript("Simple script with variables") { SimpleScriptExecutor().executeScript("useContext.kts") }
            runScript("Simple script using business package") { SimpleScriptExecutor().executeScript("useContextAndBusiness.kts") }
            runScript("Simple script using business and common package", "This test will fail on fat jar, because the commons module isn't unpacked.") { SimpleScriptExecutor().executeScript("useContextAndBusinessAndCommons.kts") }
            runScript("ScriptExecutorWithCustomizedScriptingHost") {
                ScriptExecutorWithCustomizedScriptingHost().executeScript()
            }
            System.exit(0) // Not an elegant way, but here it is enough.
        }

        private fun runScript(name: String, msg: String? = null, block: () -> Unit) {
            log.info { "******************************************************************************" }
            log.info { "*** Running test $name" }
            if (msg != null) {
                log.info { "*** $msg" }
            }
            log.info { "******************************************************************************" }
            block()
        }
    }
}

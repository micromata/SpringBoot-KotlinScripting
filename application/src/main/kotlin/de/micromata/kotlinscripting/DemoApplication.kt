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
            runScript( "SIMPLE_1", "Hello world") { SimpleScriptExecutor().executeScript("helloWorld.kts") }
            runScript("SIMPLE_2", "Simple script with variables") { SimpleScriptExecutor().executeScript("useContext.kts") }
            runScript("SIMPLE_3", "Simple script using business package") { SimpleScriptExecutor().executeScript("useContextAndBusiness.kts") }
            runScript(
                "SIMPLE_4",
                "Simple script using business and common package (OK)",
                "This test will fail on fat jar, because the commons module isn't unpacked (OK)."
            ) { SimpleScriptExecutor().executeScript("useContextAndBusinessAndIndirectCommons.kts") }
            runScript(
                "CUST_OK",
                "ScriptExecutorWithCustomizedScriptingHost",
                "Using ThreadLocal, context and execution with timeout.",
            ) {
                ScriptExecutorWithCustomizedScriptingHost().executeScript("useContextAndThreadLocal.kts")
            }
            runScript(
                "CUST_TIMEOUT",
                "ScriptExecutorWithCustomizedScriptingHost: endless loop (OK)",
                "Timeout expected after 5 seconds! (OK)",
            ) {
                ScriptExecutorWithCustomizedScriptingHost().executeScript("endlessLoop.kts")
            }
            runScript(
                "CLASSLOADER_FAIL",
                "*** ScriptExecutorWithOwnClassloader (fails for fat jar)***",
                "CustomCustomLoader is not used for loading commons (no log)! *** Any ideas on how to fix this?***",
            ) {
                ScriptExecutorWithOwnClassloader().executeScript()
            }
            runScript(
                "COPY_1",
                "ScriptExecutorWithCopiedJars, direct commons-usage",
                "common package is not extracted in fat jar, but copied to temp dir for url classloader.",
            ) {
                ScriptExecutorWithCopiedJars().executeScript("useContextAndCommons.kts")
            }
            runScript(
                "COPY_MIX_FAIL",
                "ScriptExecutorWithCopiedJars, indirect commons-usage (fails for fat jar****)",
                "common package available thru url classloader, but the mix of extracted and copied jars fails. *** Any ideas on how to fix this?***",
            ) {
                ScriptExecutorWithCopiedJars().executeScript("useContextAndBusinessAndIndirectCommons.kts")
            }
            runScript(
                "COPY_2",
                "ScriptExecutorWithCopiedJars, direct commons and indirect misc-usage",
                "common and misc packages are not extracted in fat jar, but copied to temp dir for url classloader.",
            ) {
                ScriptExecutorWithCopiedJars().executeScript("useContextAndCommonsAndIndirectMisc.kts")
            }
            System.exit(0) // Not an elegant way, but here it is enough.
        }

        private fun runScript(id: String, name: String, msg: String? = null, block: () -> Unit) {
            log.info { "*****************************************************************************************************" }
            log.info { "*** $id: Running test $name" }
            if (msg != null) {
                log.info { "*** $msg" }
            }
            log.info { "*****************************************************************************************************" }
            block()
        }
    }
}

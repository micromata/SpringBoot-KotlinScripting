import de.micromata.kotlinscripting.Constants

val sb = StringBuilder()
sb.appendLine("Hello world!")
val testVar = context.getProperty("testVariable")
if (testVar == Constants.TEST_VAR) {
    sb.appendLine("Context: $testVar (OK)")
} else {
    sb.appendLine("Context: $testVar (*** ERROR, ${Constants.TEST_VAR} expected)")
}
sb.toString()

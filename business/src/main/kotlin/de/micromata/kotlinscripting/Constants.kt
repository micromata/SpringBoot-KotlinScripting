package de.micromata.kotlinscripting

object Constants {
    const val THREADLOCAL_TEST = "Hello script! Greetings from ThreadLocal."
    const val TEST_VAR = "Hello script! Greetings from testVar."


    const val HELLO_WORLD_SCRIPT = "\"Hello world!\""
    val CHECK_SCRIPT = """
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

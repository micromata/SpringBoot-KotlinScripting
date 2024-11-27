package de.micromata.kotlinscripting.utils

import mu.KotlinLogging
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

private val log = KotlinLogging.logger {}

/**
 * Kotlin scripts don't run out of the box with spring boot. This workaround is needed.
 */
internal object JarExtractor {
    const val TEMP_DIR = "kotlin-scripting-extracted-jar"

    val extractedDir = createFixedTempDirectory().toFile()

    val runningInFatJar = JarExtractor::class.java.protectionDomain.codeSource.location.toString().startsWith("jar:")

    /**
     * The classpath to be used for the script engine.
     * It contains the copied jars.
     */
    var classpathFiles: List<File>? = null
        private set
    var classpathUrls: Array<URL>? = null
        private set

    private val copyJars = listOf(
        "commons",
        "misc",
    ).map { Regex("""$it-\d+(\.\d+)*\.jar${'$'}""") } // """commons-\d+(\.\d+)*\.jar$""",

    init {
        log.info { "Source code location: ${JarExtractor::class.java.protectionDomain.codeSource.location}" }
        if (runningInFatJar) {
            log.info { "We're running in a fat jar: ${JarExtractor::class.java.protectionDomain.codeSource.location}" }
            val classpath = System.getProperty("java.class.path")
            val jarPath = File(classpath) // Fat JAR or classpath
            extract(jarPath)
        } else {
            log.info { "We aren't running in a fat jar: ${JarExtractor::class.java.protectionDomain.codeSource.location}" }
        }
    }

    private fun extract(springBootJarFile: File) {
        log.info { "Detecting jar file: ${springBootJarFile.absolutePath}" }
        val files = mutableListOf<File>()
        files.add(springBootJarFile.absoluteFile) // Add the spring boot jar file itself. But the test COPY_MIX_FAIL will fail, why?
        JarFile(springBootJarFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    if (!entry.isDirectory) {
                        val origFile = File(entry.name)
                        if (origFile.extension == "jar" && copyJars.any { origFile.name.matches(it) }) {
                            val jarFile = File(extractedDir, origFile.name)
                            log.debug { "Copying jar file: ${origFile.name} -> ${jarFile.absolutePath}" }
                            // Extract JAR file in destination directory
                            jarFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                            files.add(jarFile.absoluteFile)
                        }
                    }
                }
            }
        }
        classpathFiles = files
        classpathUrls = files.map { it.toURI().toURL() }.toTypedArray()
    }


    fun createFixedTempDirectory(): Path {
        val systemTempDir = Paths.get(System.getProperty("java.io.tmpdir"))
        val tempDir = systemTempDir.resolve(TEMP_DIR)
        if (tempDir.exists()) {
            log.info { "Deleting temp directory: ${tempDir.absolutePathString()}" }
            tempDir.toFile().deleteRecursively()
        }
        log.info { "Creating temp directory: ${tempDir.absolutePathString()}" }
        Files.createDirectories(tempDir)
        return tempDir
    }

}

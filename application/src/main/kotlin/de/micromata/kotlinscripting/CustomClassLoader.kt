package de.micromata.kotlinscripting

import mu.KotlinLogging
import java.io.InputStream
import java.net.URL
import java.util.*

private val log = KotlinLogging.logger {}

class CustomClassLoader(parent: ClassLoader?) : ClassLoader("CustomClassLoader", parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        log.info { "loadClass(\"$name\", $resolve)" }
        return super.loadClass(name, resolve)
    }

    override fun findResource(name: String): URL? {
        val url = super.findResource(name)
        log.info { "findResource(\"$name\") -> $url" }
        return url
    }

    override fun findClass(name: String?): Class<*>? {
        val clazz = super.findClass(name)
        log.info { "findClass(\"$name\") -> $clazz" }
        return super.findClass(name)
    }

    override fun findClass(moduleName: String?, name: String?): Class<*>? {
        val clazz = super.findClass(moduleName, name)
        log.info { "findClass(moduleName=\"$moduleName\", name=\"$name\") -> $clazz" }
        return super.findClass(moduleName, name)
    }

    override fun getResource(name: String): URL? {
        val url = super.getResource(name)
        log.info { "getResource(\"$name\") -> $url" }
        return url
    }

    override fun getResourceAsStream(name: String?): InputStream? {
        log.info { "getAsResourceStream(\"$name\")" }
        return super.getResourceAsStream(name)
    }

    override fun findResources(name: String?): Enumeration<URL> {
        log.info { "findResources(\"$name\")" }
        return super.findResources(name)
    }
}

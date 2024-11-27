package de.micromata.kotlinscripting.business

/**
 * ThreadLocalStorage object for testing access of ThreadLocal variables inside scripts.
 */
object ThreadLocalStorage {
    val threadLocal = ThreadLocal.withInitial { "Default Value" }
}

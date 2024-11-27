package de.micromata.kotlinscripting.business

import de.micromata.kotlinscripting.commons.GetMessage

object Foo {
    fun bar(): String {
        return "A warm welcome from business package."
    }

    fun useCommons(): String {
        return GetMessage.getMessage()
    }
}

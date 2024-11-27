package de.micromata.kotlinscripting.commons

object GetMessage {
    fun getMessage(): String {
        return "Hello world from commons package!"
    }

    fun getMiscMessage(): String {
        return "commons package is calling misc package: " + de.micromata.kotlinscripting.misc.Misc.getMessage()
    }
}

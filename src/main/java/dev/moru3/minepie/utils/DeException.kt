package dev.moru3.minepie.utils

class DeException<R>(runnable: ()->R) {
    private var exception: Exception? = null
    private var result: R? = null
    private var isException = false

    fun thrown(runnable: (Exception) -> Unit): DeException<R> {
        if(isException) { exception?.also(runnable) }
        return this
    }

    fun run(runnable: (R?)->Unit): DeException<R> {
        if(isException.not()) {
            runnable.invoke(result)
        }
        return this
    }

    init {
        try { result = runnable.invoke() } catch (ex: Exception) { exception = ex;isException = true }
    }
}
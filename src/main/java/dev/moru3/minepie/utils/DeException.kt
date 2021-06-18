package dev.moru3.minepie.utils

class DeException<R>(runnable: ()->R): IDeException<R> {
    private lateinit var exception: Exception
    private var result: R? = null
    private var isException = false

    override fun thrown(runnable: (Exception) -> Unit): DeException<R> {
        if(isException) { exception.also(runnable) }
        return this
    }

    fun runAllowNull(runnable: (R?)->Unit): DeException<R> {
        try {
            if(isException.not()) { result.also(runnable::invoke) }
        } catch (_: Exception) { /*パス*/ }
        return this
    }

    override fun run(runnable: (R)->Unit): DeException<R> {
        try {
            if(isException.not()) { result?.also(runnable::invoke) }
        } catch (_: Exception) { /*パス*/ }
        return this
    }

    init {
        try { result = runnable.invoke() } catch (ex: Exception) { exception = ex;isException = true }
    }
}
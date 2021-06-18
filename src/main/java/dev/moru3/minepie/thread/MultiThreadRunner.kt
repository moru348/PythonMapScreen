package dev.moru3.minepie.thread

class MultiThreadRunner(runnable: () -> Unit) {
    init {
        Thread {
            runnable.invoke()
            Thread.currentThread().interrupt()
        }.start()
    }
}
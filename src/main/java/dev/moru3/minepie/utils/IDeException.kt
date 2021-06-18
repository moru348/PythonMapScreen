package dev.moru3.minepie.utils

interface IDeException<R> {
    fun thrown(runnable: (Exception) -> Unit): IDeException<R>

    fun run(runnable: (R)->Unit): DeException<R>
}
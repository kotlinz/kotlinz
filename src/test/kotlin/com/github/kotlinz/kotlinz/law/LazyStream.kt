package com.github.kotlinz.kotlinz.law

/** An infinite, lazily linked stream used to observe knot-tying; equality compares a finite prefix. */
class LazyStream(val head: Int, val tail: () -> LazyStream) {
    fun take(n: Int): List<Int> = generateSequence(this) { it.tail() }.take(n).map { it.head }.toList()

    override fun equals(other: Any?): Boolean = other is LazyStream && take(PREFIX) == other.take(PREFIX)
    override fun hashCode(): Int = take(PREFIX).hashCode()
    override fun toString(): String = "LazyStream(${take(PREFIX)}...)"

    private companion object {
        const val PREFIX = 8
    }
}

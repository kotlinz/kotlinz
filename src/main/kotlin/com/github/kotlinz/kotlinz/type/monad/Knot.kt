package com.github.kotlinz.kotlinz.type.monad

/**
 * A write-once cell used to tie recursive knots in strict code: [thunk] reads the value set later
 * by [tie] and throws if it is forced before that.
 */
internal class Knot<A> {
  private var tied = false
  private var value: A? = null

  /** Reads the tied value; throws [IllegalStateException] if forced before [tie]. */
  val thunk: () -> A = { read() }

  /** Sets the value and returns it. */
  fun tie(a: A): A {
    value = a
    tied = true
    return a
  }

  @Suppress("UNCHECKED_CAST")
  private fun read(): A {
    check(tied) { "mfix: the recursive value was forced before it was defined" }
    return value as A
  }

  companion object {
    /** Runs `f` with a thunk of its own result. */
    fun <A> fix(f: (() -> A) -> A): A = Knot<A>().let { k -> k.tie(f(k.thunk)) }
  }
}

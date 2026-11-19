package com.github.kotlinz.kotlinz.type.show

/**
 * A type class for rendering values of type [A] as human-readable strings.
 *
 * Unlike [Any.toString], an instance is chosen explicitly and derived instances are built from element
 * instances, so nested values render consistently (e.g. strings are quoted inside containers).
 *
 * ```kotlin
 * Maybe.show(Int.show).show(Maybe.Just(1))                       // "Just(1)"
 * Either.show(String.show, Int.show).show(Either.Left("e"))      // "Left(\"e\")"
 * ```
 */
interface Show<A> {
  /** Renders [a] as a string. */
  fun show(a: A): String

  companion object {
    /** An instance that delegates to [Any.toString]. */
    fun <A> fromToString(): Show<A> = ToStringShow()
    /** An instance built from the rendering function [f]. */
    fun <A> by(f: (A) -> String): Show<A> = object: Show<A> {
      override fun show(a: A): String = f(a)
    }
    /** Renders lists as `[a, b, c]` using [showA] for the elements. */
    fun <A> list(showA: Show<A>): Show<List<A>> = object: ListShow<A> { override val showA = showA }
  }
}

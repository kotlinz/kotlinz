package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf

/**
 * A read-only view of exactly one `A` inside an `S` (a function `S -> A` as an optic).
 *
 * Profunctor encoding: a `Getter` lifts any [Forget] through the structure via [runGetter], with
 * no monoid needed. Every [Lens] (hence every Iso) is a `Getter`, and every `Getter` is a [Fold].
 *
 * ```kotlin
 * val length = Getter { s: String -> s.length }
 * length.view("abc")                                       // 3
 * (Lens.first<String, String, Int>() andThen length).view("ab" to 1) // 2
 * ```
 */
interface Getter<S, A> : Fold<S, A> {
  /** Runs [pab] on the single focus. */
  fun <R> runGetter(pab: Forget<R, A, A>): Forget<R, S, S>

  override fun <R> runFold(monoid: MonoidOf<R>, pab: Forget<R, A, A>): Forget<R, S, S> = runGetter(pab)
}

/** Builds a [Getter] from a function. */
fun <S, A> Getter(get: (S) -> A): Getter<S, A> = object : Getter<S, A> {
  override fun <R> runGetter(pab: Forget<R, A, A>): Forget<R, S, S> = Forget { s -> pab.run(get(s)) }
}

/** Reads the focus. */
fun <S, A> Getter<S, A>.view(s: S): A = runGetter(Forget<A, A, A> { it }).run(s)

/** Composes with [other], viewing first through this getter and then through [other]. */
infix fun <S, A, C> Getter<S, A>.andThen(other: Getter<A, C>): Getter<S, C> = object : Getter<S, C> {
  override fun <R> runGetter(pab: Forget<R, C, C>): Forget<R, S, S> = this@andThen.runGetter(other.runGetter(pab))
}

package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.type.Foldable

/**
 * A read-only view of zero or more `A`s inside an `S`.
 *
 * Profunctor encoding: a `Fold` lifts a [Forget] (whose output type is phantom) through the
 * structure, combining the results with a monoid, via [runFold]. Every [Traversal] and [Getter] is
 * a `Fold`. Use the [Fold] factories and the [foldMapOf] / [toListOf] / [preview] helpers.
 *
 * ```kotlin
 * val digits = Fold { s: String -> s.filter(Char::isDigit).toList() }
 * digits.toListOf("a1b2")                                        // [1, 2]
 * digits.preview("abc")                                          // None
 * digits.foldMapOf(MonoidOf(0) { a, b -> a + b }, { it.digitToInt() }, "a1b2") // 3
 * ```
 */
interface Fold<S, A> {
  /** Runs [pab] on every focus, combining the results with [monoid]. */
  fun <R> runFold(monoid: MonoidOf<R>, pab: Forget<R, A, A>): Forget<R, S, S>

  /** Hosts fold constructors such as [fromFoldable]. */
  companion object
}

/** Builds a [Fold] from a function listing the foci. */
fun <S, A> Fold(toList: (S) -> List<A>): Fold<S, A> = object : Fold<S, A> {
  override fun <R> runFold(monoid: MonoidOf<R>, pab: Forget<R, A, A>): Forget<R, S, S> =
    Forget { s -> toList(s).fold(monoid.empty) { acc, a -> monoid.combine(acc, pab.run(a)) } }
}

/** The fold over every element of a [foldable] container. */
fun <F, A> Fold.Companion.fromFoldable(foldable: Foldable<F>): Fold<K1<F, A>, A> = Fold { fa: K1<F, A> -> foldable.toList(fa) }

/** Maps every focus with [f] and combines the results with [monoid]. */
fun <S, A, R> Fold<S, A>.foldMapOf(monoid: MonoidOf<R>, f: (A) -> R, s: S): R = runFold(monoid, Forget(f)).run(s)

/** Collects every focus in order. */
fun <S, A> Fold<S, A>.toListOf(s: S): List<A> = foldMapOf(MonoidOf(listOf()) { a, b -> a + b }, { listOf(it) }, s)

/** Returns the first focus, if any. */
fun <S, A> Fold<S, A>.preview(s: S): Maybe<A> = foldMapOf(firstJust(), { Maybe.Just(it) }, s)

/** Composes with [other], folding first through this fold and then through [other]. */
infix fun <S, A, C> Fold<S, A>.andThen(other: Fold<A, C>): Fold<S, C> = object : Fold<S, C> {
  override fun <R> runFold(monoid: MonoidOf<R>, pab: Forget<R, C, C>): Forget<R, S, S> =
    this@andThen.runFold(monoid, other.runFold(monoid, pab))
}

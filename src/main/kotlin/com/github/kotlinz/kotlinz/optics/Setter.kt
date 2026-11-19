package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * A write-only focus on zero or more `A`s inside an `S` (polymorphic: mapping them to `B`s yields a `T`).
 *
 * Profunctor encoding: a `Setter` lifts a [Function1] (the function profunctor) from `A -> B` to
 * `S -> T` via [runSetter]. Every [Traversal] (hence every Optional, Lens, Prism and Iso) is a `Setter`.
 *
 * Laws:
 * - Identity: `over { it }(s) == s`
 * - Composition: `over(f)(over(g)(s)) == over { f(g(it)) }(s)`
 * - Idempotence: `set(b)(set(b)(s)) == set(b)(s)`
 *
 * ```kotlin
 * val values = Setter { f: (Int) -> String -> { m: Map<String, Int> -> m.mapValues { f(it.value) } } }
 * values.over { "#$it" }(mapOf("a" to 1))   // {a=#1}
 * values.set("x")(mapOf("a" to 1, "b" to 2)) // {a=x, b=x}
 * ```
 */
interface Setter<S, T, A, B> {
  /** Lifts the function [pab] on foci to a function on the whole structure. */
  fun runSetter(pab: Function1<A, B>): Function1<S, T>

  /** Hosts setter constructors such as [fromFunctor]. */
  companion object
}

/** Builds a [Setter] from a function that maps every focus. */
fun <S, T, A, B> Setter(over: ((A) -> B) -> (S) -> T): Setter<S, T, A, B> = object : Setter<S, T, A, B> {
  override fun runSetter(pab: Function1<A, B>): Function1<S, T> = Function1(over(pab.run))
}

/** The setter over every element of a [functor]. */
fun <F, A, B> Setter.Companion.fromFunctor(functor: Functor<F>): Setter<K1<F, A>, K1<F, B>, A, B> =
  Setter { f: (A) -> B -> { fa: K1<F, A> -> functor.fmap(f, fa) } }

/** Maps every focus with [f]. */
fun <S, T, A, B> Setter<S, T, A, B>.over(f: (A) -> B): (S) -> T = runSetter(Function1(f)).run

/** Replaces every focus with [b]. */
fun <S, T, A, B> Setter<S, T, A, B>.set(b: B): (S) -> T = over { b }

/** Composes with [other], focusing first through this setter and then through [other]. */
infix fun <S, T, A, B, C, D> Setter<S, T, A, B>.andThen(other: Setter<A, B, C, D>): Setter<S, T, C, D> =
  object : Setter<S, T, C, D> {
    override fun runSetter(pab: Function1<C, D>): Function1<S, T> = this@andThen.runSetter(other.runSetter(pab))
  }

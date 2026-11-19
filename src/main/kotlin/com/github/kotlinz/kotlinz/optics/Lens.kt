package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.forget.ForgetStrong
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Strong
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/**
 * A focus on exactly one `A` inside an `S` (polymorphic: replacing it with a `B` yields a `T`).
 *
 * Profunctor encoding: a `Lens` lifts any [Strong] profunctor value `P<A, B>` to `P<S, T>` via the
 * generic method [runStrong]. Use the [Lens] factory and the [get] / [set] / [modify] helpers.
 *
 * Laws:
 * - Get-set: `get(set(b)(s)) == b`
 * - Set-get: `set(get(s))(s) == s`
 * - Set-set: `set(b2)(set(b1)(s)) == set(b2)(s)`
 *
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 * val age = Lens({ p: Person -> p.age }, { p: Person, a: Int -> p.copy(age = a) })
 * age.get(Person("Ann", 30))                 // 30
 * age.set(31)(Person("Ann", 30))             // Person(Ann, 31)
 * age.modify { it + 1 }(Person("Ann", 30))   // Person(Ann, 31)
 * ```
 */
interface Lens<S, T, A, B> {
  /** Lifts [pab] through this lens using [strong]. */
  fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>

  /** Hosts ready-made lenses such as [first] and [second]. */
  companion object
}

/** Builds a [Lens] from a getter and a setter. */
fun <S, T, A, B> Lens(get: (S) -> A, set: (S, B) -> T): Lens<S, T, A, B> = object : Lens<S, T, A, B> {
  override fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> =
    strong.dimap({ s: S -> Pair(get(s), s) }, { (b, s): Pair<B, S> -> set(s, b) }, strong.first<A, B, S>(pab))
}

/** The lens on the first component of a pair. */
fun <A, B, C> Lens.Companion.first(): Lens<Pair<A, C>, Pair<B, C>, A, B> = object : Lens<Pair<A, C>, Pair<B, C>, A, B> {
  override fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>) = strong.first<A, B, C>(pab)
}

/** The lens on the second component of a pair. */
fun <A, B, C> Lens.Companion.second(): Lens<Pair<C, A>, Pair<C, B>, A, B> = object : Lens<Pair<C, A>, Pair<C, B>, A, B> {
  override fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>) = strong.second<A, B, C>(pab)
}

/** Reads the focus. */
fun <S, T, A, B> Lens<S, T, A, B>.get(s: S): A =
  Forget.narrow(runStrong(object : ForgetStrong<A> {}, Forget<A, A, B> { it })).run(s)

/** Replaces the focus with [b]. */
fun <S, T, A, B> Lens<S, T, A, B>.set(b: B): (S) -> T = modify { b }

/** Modifies the focus with [f]. */
fun <S, T, A, B> Lens<S, T, A, B>.modify(f: (A) -> B): (S) -> T =
  Function1.narrow(runStrong(object : Function1Strong {}, Function1(f))).run

/** Composes with [other], focusing first through this lens and then through [other]. */
infix fun <S, T, A, B, C, D> Lens<S, T, A, B>.andThen(other: Lens<A, B, C, D>): Lens<S, T, C, D> = object : Lens<S, T, C, D> {
  override fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, C, D>): K3<P, F, S, T> =
    this@andThen.runStrong(strong, other.runStrong(strong, pab))
}

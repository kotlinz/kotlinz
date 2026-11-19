package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.forget.ForgetProfunctor
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Profunctor
import com.github.kotlinz.kotlinz.data.tagged.Tagged
import com.github.kotlinz.kotlinz.data.tagged.TaggedProfunctor
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/**
 * An isomorphism between `S` and `A` (polymorphic: `T` and `B` are the rebuilt types).
 *
 * Every `Iso` is a [Lens], so it composes with other optics through subtyping.
 *
 * Profunctor encoding: an `Iso` lifts any profunctor value `P<A, B>` to `P<S, T>`. Kotlin lacks
 * rank-2 types, so the quantification over the profunctor is the generic method [runProfunctor].
 * Use the [Iso] factory and the [get] / [reverseGet] / [reverse] / [modify] helpers instead of
 * calling it directly.
 *
 * Laws:
 * - `reverseGet(get(s)) == s`
 * - `get(reverseGet(b)) == b`
 *
 * ```kotlin
 * val chars = Iso({ s: String -> s.toList() }, { cs: List<Char> -> cs.joinToString("") })
 * chars.get("ab")                              // [a, b]
 * chars.reverseGet(listOf('x'))                // "x"
 * chars.modify { it.reversed() }("abc")        // "cba"
 * ```
 */
interface Iso<S, T, A, B> : Lens<S, T, A, B> {
  /** Lifts [pab] through this isomorphism using only [profunctor]. */
  fun <P, F> runProfunctor(profunctor: Profunctor<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>

  override fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> = runProfunctor(strong, pab)

  /** Hosts ready-made isomorphisms such as [id]. */
  companion object
}

/** Builds an [Iso] from a pair of mutually inverse functions. */
fun <S, T, A, B> Iso(get: (S) -> A, reverseGet: (B) -> T): Iso<S, T, A, B> = object : Iso<S, T, A, B> {
  override fun <P, F> runProfunctor(profunctor: Profunctor<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> =
    profunctor.dimap(get, reverseGet, pab)
}

/** The identity isomorphism. */
fun <S> Iso.Companion.id(): Iso<S, S, S, S> = Iso({ s: S -> s }, { s: S -> s })

/** Views `S` as `A`. */
fun <S, T, A, B> Iso<S, T, A, B>.get(s: S): A =
  Forget.narrow(runProfunctor(object : ForgetProfunctor<A> {}, Forget<A, A, B> { it })).run(s)

/** Builds a `T` back from a `B`. */
fun <S, T, A, B> Iso<S, T, A, B>.reverseGet(b: B): T =
  Tagged.narrow(runProfunctor(object : TaggedProfunctor {}, Tagged<A, B>(b))).value

/** Flips the isomorphism. */
fun <S, T, A, B> Iso<S, T, A, B>.reverse(): Iso<B, A, T, S> = Iso({ b: B -> reverseGet(b) }, { s: S -> get(s) })

/** Modifies the `A` view of an `S` with [f]. */
fun <S, T, A, B> Iso<S, T, A, B>.modify(f: (A) -> B): (S) -> T =
  Function1.narrow(runProfunctor(object : Function1Profunctor {}, Function1(f))).run

/** Composes with [other], focusing first through this optic and then through [other]. */
infix fun <S, T, A, B, C, D> Iso<S, T, A, B>.andThen(other: Iso<A, B, C, D>): Iso<S, T, C, D> = object : Iso<S, T, C, D> {
  override fun <P, F> runProfunctor(profunctor: Profunctor<P, F>, pab: K3<P, F, C, D>): K3<P, F, S, T> =
    this@andThen.runProfunctor(profunctor, other.runProfunctor(profunctor, pab))
}

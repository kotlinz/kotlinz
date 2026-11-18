package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * The traced (cowriter) comonad: a function `M -> A` from an accumulated monoidal value `M`.
 *
 * Use it for computations that look "ahead" by appending to a monoidal position, e.g. querying a
 * value relative to the current offset. Has Functor, Copointed and Comonad instances; all require
 * the Monoid class of `M`.
 *
 * ```kotlin
 * val traced = Traced(StringMonoid::class.java) { m: StringMonoid -> m.value.length }
 * traced.extract()                  // 0 (runs at mzero)
 * traced.trace(StringMonoid("abc")) // 3
 * ```
 *
 * @property type the Monoid class of `M`, used to obtain `mzero` and `op`.
 * @property run the traced function.
 */
class Traced<M: Monoid<*, M>, A>(val type: Class<M>, val run: (M) -> A): K2<Traced.T, M, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <M: Monoid<*, M>, A> narrow(v: K1<K1<T, M>, A>): Traced<M, A> = v as Traced<M, A>
  }

  /** Runs the function at the accumulated value `m`. */
  fun trace(m: M): A = run(m)
  /** Runs the function at a value derived from the current focus. */
  fun traces(f: (A) -> M): A = run(f(extract()))
  /** Pairs each result with the accumulated value it was computed at. */
  fun listen(): Traced<M, Pair<A, M>> = Traced(type) { m -> Pair(run(m), m) }
  /** Transforms the accumulated value before running the function. */
  fun censor(f: (M) -> M): Traced<M, A> = Traced(type) { m -> run(f(m)) }

  // Comonad
  /** Maps the result of the function. */
  infix fun <B> fmap(f: (A) -> B): Traced<M, B> = comonad.fmap(f, this)
  /** Runs the function at `mzero`. */
  fun extract(): A = comonad.extract(this)
  /** Returns a nested Traced whose inner trace is appended to the outer one. */
  fun duplicate(): Traced<M, K1<K1<T, M>, A>> = comonad.duplicate(this)
  /** Computes a new Traced by applying `f` to the Traced shifted by every accumulated value. */
  infix fun <B> extend(f: (K1<K1<T, M>, A>) -> B): Traced<M, B> = comonad.extend(f, this)
  private val comonad = object: TracedComonad<M> { override val type = this@Traced.type }
}

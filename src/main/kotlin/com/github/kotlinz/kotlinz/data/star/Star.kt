package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A function into a functor, `(A) -> F<B>`, viewed as a profunctor (the "Star" of [F]).
 *
 * Running an optic on a `Star` over an `Applicative<F>` yields an effectful traversal (`traverseOf`).
 *
 * Instances: [StarProfunctor] and [StarStrong] (need `Functor<F>`), [StarChoice] and [StarTraversing]
 * (need `Applicative<F>`), [StarClosed] (needs `Distributive<F>`).
 *
 * ```kotlin
 * val half = Star { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }
 * val profunctor = object : StarProfunctor<Maybe.T> { override val functor = object : MaybeFunctor {} }
 * Star.narrow(profunctor.rmap({ n: Int -> n + 1 }, half))(8) // Just(5)
 * ```
 *
 * @property run the underlying effectful function.
 */
class Star<F, A, B>(val run: (A) -> K1<F, B>): K3<Star.T, F, A, B> {
  /** Witness type used to encode `Star` as a higher-kinded `K3<Star.T, F, A, B>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Star]. */
    fun <F, A, B> narrow(v: K1<K2<T, F, A>, B>): Star<F, A, B> = v as Star<F, A, B>
  }

  /** Applies the function to [a]. */
  operator fun invoke(a: A): K1<F, B> = run(a)

  /** Pre-composes [f] onto the input (needs no functor). */
  infix fun <C> lmap(f: (C) -> A): Star<F, C, B> = Star { c -> run(f(c)) }
}

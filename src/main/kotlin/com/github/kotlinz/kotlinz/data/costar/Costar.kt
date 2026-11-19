package com.github.kotlinz.kotlinz.data.costar

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * A function out of a functor, `(F<A>) -> B`, viewed as a profunctor (the "Costar" of [F]).
 *
 * Instances: [CostarProfunctor] (needs `Functor<F>`).
 *
 * ```kotlin
 * val sum = Costar { xs: K1<ListK.T, Int> -> ListK.narrow(xs).value.sum() }
 * val profunctor = object : CostarProfunctor<ListK.T> { override val functor = object : ListKFunctor {} }
 * Costar.narrow(profunctor.lmap({ s: String -> s.length }, sum))(listKOf("a", "bc")) // 3
 * ```
 *
 * @property run the underlying function.
 */
class Costar<F, A, B>(val run: (K1<F, A>) -> B): K3<Costar.T, F, A, B> {
  /** Witness type used to encode `Costar` as a higher-kinded `K3<Costar.T, F, A, B>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Costar]. */
    fun <F, A, B> narrow(v: K1<K2<T, F, A>, B>): Costar<F, A, B> = v as Costar<F, A, B>
  }

  /** Applies the function to [fa]. */
  operator fun invoke(fa: K1<F, A>): B = run(fa)

  /** Post-composes [g] onto the output (needs no functor). */
  infix fun <C> rmap(g: (B) -> C): Costar<F, A, C> = Costar { fa -> g(run(fa)) }
}

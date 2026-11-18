package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The cofree comonad: a value [head] annotated with an F-shaped structure of further Cofree nodes.
 *
 * Models non-empty, possibly infinite trees or streams (e.g. `Cofree<ListK.T, A>` is a rose tree,
 * `Cofree<Identity.T, A>` an infinite stream). The tail is evaluated lazily, so infinite structures can be
 * described with [unfold] / [coiter]. Has instances for Functor, Copointed and Comonad, all requiring a
 * Functor of F (see [CofreeComonad]).
 *
 * ```kotlin
 * val naturals = Cofree.coiter(object : IdentityFunctor {}, 0) { Identity(it + 1) }
 * naturals.head // 0
 * Cofree.comonad(object : IdentityFunctor {}).extend({ Cofree.narrow(it).head * 2 }, naturals)
 * ```
 */
class Cofree<F, A>(val head: A, tail: () -> K1<F, Cofree<F, A>>): K2<Cofree.T, F, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Cofree]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Cofree<F, A> = v as Cofree<F, A>

    /** Builds a Cofree from [seed], producing each head and the next seeds with [f]. */
    fun <F, B, A> unfold(functor: Functor<F>, seed: B, f: (B) -> Pair<A, K1<F, B>>): Cofree<F, A> {
      val (head, next) = f(seed)
      return Cofree(head) { functor.fmap({ b: B -> unfold(functor, b, f) }, next) }
    }

    /** Builds a Cofree by repeatedly applying [f], keeping every intermediate value as a head. */
    fun <F, A> coiter(functor: Functor<F>, seed: A, f: (A) -> K1<F, A>): Cofree<F, A> =
      unfold(functor, seed) { a -> Pair(a, f(a)) }

    /** Returns the Comonad instance for Cofree over [functor]. */
    fun <F> comonad(functor: Functor<F>): CofreeComonad<F> = CofreeComonad.of(functor)
  }

  /** The lazily evaluated sub-structure. */
  val tail: K1<F, Cofree<F, A>> by lazy(tail)

  /** Returns the [head]. */
  fun extract(): A = head
}

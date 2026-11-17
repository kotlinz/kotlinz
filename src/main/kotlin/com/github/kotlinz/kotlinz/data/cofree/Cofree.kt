package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

// The tail is evaluated lazily, so infinite structures can be described with unfold / coiter.
class Cofree<F, A>(val head: A, tail: () -> K1<F, Cofree<F, A>>): K2<Cofree.T, F, A> {
  class T

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Cofree<F, A> = v as Cofree<F, A>

    fun <F, B, A> unfold(functor: Functor<F>, seed: B, f: (B) -> Pair<A, K1<F, B>>): Cofree<F, A> {
      val (head, next) = f(seed)
      return Cofree(head) { functor.fmap({ b: B -> unfold(functor, b, f) }, next) }
    }

    fun <F, A> coiter(functor: Functor<F>, seed: A, f: (A) -> K1<F, A>): Cofree<F, A> =
      unfold(functor, seed) { a -> Pair(a, f(a)) }

    fun <F> comonad(functor: Functor<F>): CofreeComonad<F> = CofreeComonad.of(functor)
  }

  val tail: K1<F, Cofree<F, A>> by lazy(tail)

  fun extract(): A = head
}

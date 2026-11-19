package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [Chain]. */
interface ChainTraversable: Traversable<Chain.T>, ChainFunctor, ChainFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<Chain.T, A>): K1<G, K1<Chain.T, B>> =
    foldr(applicative.pure(Chain.empty<B>()), { a: A, acc: K1<G, K1<Chain.T, B>> ->
      applicative.ap(applicative.fmap(prepend(), f(a)), acc)
    }, v)

  private fun <B> prepend(): (B) -> (K1<Chain.T, B>) -> K1<Chain.T, B> = { b -> { chain -> Chain.narrow(chain) prepend b } }
}

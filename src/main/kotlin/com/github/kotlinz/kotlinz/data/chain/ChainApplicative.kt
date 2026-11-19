package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Chain] with cartesian-product semantics. */
interface ChainApplicative: Applicative<Chain.T>, ChainFunctor {
  override fun <A> pure(v: A): Chain<A> = Chain.one(v)

  override fun <A, B> ap(f: K1<Chain.T, (A) -> B>, v: K1<Chain.T, A>): Chain<B> =
    Chain.narrow(f).fold(Chain.empty()) { acc, g -> acc + fmap(g, v) }
}

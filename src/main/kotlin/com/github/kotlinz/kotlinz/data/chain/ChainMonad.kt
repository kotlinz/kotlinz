package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Chain]; [bind] concatenates the produced chains in O(1) each. */
interface ChainMonad: Monad<Chain.T>, ChainApplicative {
  override fun <A> join(v: K1<Chain.T, K1<Chain.T, A>>): Chain<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<Chain.T, B>, v: K1<Chain.T, A>): Chain<B> =
    Chain.narrow(v).fold(Chain.empty()) { acc, a -> acc + Chain.narrow(f(a)) }
}

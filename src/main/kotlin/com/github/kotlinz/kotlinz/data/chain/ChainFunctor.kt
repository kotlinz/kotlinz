package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Chain]. */
interface ChainFunctor: Functor<Chain.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Chain.T, A>): Chain<B> = Chain.fromList(Chain.narrow(v).map(f))
}

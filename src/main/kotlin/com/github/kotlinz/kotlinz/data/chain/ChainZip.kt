package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [Chain]; the result is as long as the shorter chain. */
interface ChainZip: Zip<Chain.T>, ChainSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<Chain.T, A>, b: K1<Chain.T, B>): Chain<C> =
    Chain.fromList(Chain.narrow(a).toList().zip(Chain.narrow(b).toList(), f))

  override fun <A, B> zip(a: K1<Chain.T, A>, b: K1<Chain.T, B>): Chain<Pair<A, B>> = Chain.narrow(super.zip(a, b))
}

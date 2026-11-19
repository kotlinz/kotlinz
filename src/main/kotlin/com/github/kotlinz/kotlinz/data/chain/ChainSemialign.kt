package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.alignLists
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [Chain]; the result is as long as the longer chain. */
interface ChainSemialign: Semialign<Chain.T>, ChainFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<Chain.T, A>, b: K1<Chain.T, B>): Chain<C> =
    Chain.fromList(alignLists(Chain.narrow(a).toList(), Chain.narrow(b).toList()).map(f))

  override fun <A, B> align(a: K1<Chain.T, A>, b: K1<Chain.T, B>): Chain<Ior<A, B>> = Chain.narrow(super.align(a, b))
}

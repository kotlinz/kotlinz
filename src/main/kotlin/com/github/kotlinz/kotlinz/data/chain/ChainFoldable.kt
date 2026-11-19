package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [Chain]. */
interface ChainFoldable: Foldable<Chain.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<Chain.T, A>): B = Chain.narrow(v).fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<Chain.T, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<Chain.T, A>): List<A> = Chain.narrow(v).toList()
  override fun <A> length(v: K1<Chain.T, A>): Int = Chain.narrow(v).size
}

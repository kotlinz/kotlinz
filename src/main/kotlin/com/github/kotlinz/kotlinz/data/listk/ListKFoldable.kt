package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [ListK]. */
interface ListKFoldable: Foldable<ListK.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<ListK.T, A>): B = ListK.narrow(v).value.fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<ListK.T, A>): B = ListK.narrow(v).value.foldRight(initial, f)
  override fun <A> toList(v: K1<ListK.T, A>): List<A> = ListK.narrow(v).value
  override fun <A> length(v: K1<ListK.T, A>): Int = ListK.narrow(v).value.size
}

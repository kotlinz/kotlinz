package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [ListZipper], visiting every element in list order. */
interface ListZipperFoldable: Foldable<ListZipper.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<ListZipper.T, A>): B = toList(v).fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<ListZipper.T, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<ListZipper.T, A>): List<A> = ListZipper.narrow(v).toList()
  override fun <A> length(v: K1<ListZipper.T, A>): Int = ListZipper.narrow(v).size
}

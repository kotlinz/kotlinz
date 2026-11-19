package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 instance for [ListZipper]: traverses with an [Apply] in list order, keeping the focus position. */
interface ListZipperTraversable1: Traversable1<ListZipper.T>, ListZipperTraversable, ListZipperFoldable1 {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<ListZipper.T, A>): K1<G, K1<ListZipper.T, B>> =
    traverseZipper(apply, f, ListZipper.narrow(v))
}

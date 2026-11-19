package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListTraversable1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable instance for [ListZipper], running the effects in list order and keeping the focus position. */
interface ListZipperTraversable: Traversable<ListZipper.T>, ListZipperFunctor, ListZipperFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<ListZipper.T, A>): K1<G, K1<ListZipper.T, B>> =
    traverseZipper(applicative, f, ListZipper.narrow(v))
}

/** Traverses all elements of [zipper] in list order with an [Apply], then restores the focus position. */
internal fun <G, A, B> traverseZipper(apply: Apply<G>, f: (A) -> K1<G, B>, zipper: ListZipper<A>): K1<G, K1<ListZipper.T, B>> {
  val traversed = object: NonEmptyListTraversable1 {}.traverse1(apply, f, zipper.toNonEmptyList())
  return apply.fmap({ list: K1<NonEmptyList.T, B> -> refocus(NonEmptyList.narrow(list).toList(), zipper.index) }, traversed)
}

private fun <B> refocus(all: List<B>, index: Int): K1<ListZipper.T, B> = ListZipper(all.take(index), all[index], all.drop(index + 1))

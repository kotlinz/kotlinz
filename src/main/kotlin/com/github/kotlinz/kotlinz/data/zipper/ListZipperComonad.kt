package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [ListZipper]; [duplicate] yields the zipper focused at every position. */
interface ListZipperComonad: Comonad<ListZipper.T>, ListZipperCopointed, ListZipperFunctor {
  override fun <A> duplicate(v: K1<ListZipper.T, A>): ListZipper<K1<ListZipper.T, A>> {
    val zipper = ListZipper.narrow(v)
    val lefts = moves(zipper) { it.left() }.reversed()
    return ListZipper(lefts, zipper, moves(zipper) { it.right() })
  }

  override fun <A, B> extend(f: (K1<ListZipper.T, A>) -> B, v: K1<ListZipper.T, A>): ListZipper<B> = ListZipper.narrow(super.extend(f, v))

  private fun <A> moves(start: ListZipper<A>, step: (ListZipper<A>) -> Maybe<ListZipper<A>>): List<K1<ListZipper.T, A>> =
    generateSequence(justOrNull(step(start))) { justOrNull(step(it)) }.toList()

  private fun <A> justOrNull(m: Maybe<A>): A? = (m as? Maybe.Just)?.value
}

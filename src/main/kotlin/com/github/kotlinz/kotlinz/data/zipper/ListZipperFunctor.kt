package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [ListZipper], keeping the focus position. */
interface ListZipperFunctor: Functor<ListZipper.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<ListZipper.T, A>): ListZipper<B> {
    val zipper = ListZipper.narrow(v)
    return ListZipper(zipper.lefts.map(f), f(zipper.focus), zipper.rights.map(f))
  }
}

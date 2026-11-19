package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [ListZipper]; extracts the focus. */
interface ListZipperCopointed: Copointed<ListZipper.T> {
  override fun <A> extract(v: K1<ListZipper.T, A>): A = ListZipper.narrow(v).focus
}

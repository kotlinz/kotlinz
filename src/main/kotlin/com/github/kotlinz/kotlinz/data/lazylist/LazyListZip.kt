package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.lazylist.LazyList.Cell
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [LazyList]; pairs elements lazily and truncates to the shorter list. */
interface LazyListZip: Zip<LazyList.T>, LazyListSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<LazyList.T, A>, b: K1<LazyList.T, B>): LazyList<C> {
    val xs = LazyList.narrow(a)
    val ys = LazyList.narrow(b)
    return LazyList.of {
      val x = xs.step
      val y = if (x is Cell.Cons) ys.step else Cell.Nil()
      if (x is Cell.Cons && y is Cell.Cons) Cell.Cons(f(x.head, y.head), zipWith(f, x.tail, y.tail)) else Cell.Nil()
    }
  }

  override fun <A, B> zip(a: K1<LazyList.T, A>, b: K1<LazyList.T, B>): LazyList<Pair<A, B>> = LazyList.narrow(super.zip(a, b))
}

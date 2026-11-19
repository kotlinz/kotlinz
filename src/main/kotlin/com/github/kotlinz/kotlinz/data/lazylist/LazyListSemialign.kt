package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.lazylist.LazyList.Cell
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [LazyList]; pairs elements lazily and keeps the leftover elements of the longer list. */
interface LazyListSemialign: Semialign<LazyList.T>, LazyListFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<LazyList.T, A>, b: K1<LazyList.T, B>): LazyList<C> {
    val xs = LazyList.narrow(a)
    val ys = LazyList.narrow(b)
    return LazyList.of { alignStep(f, xs.step, ys.step) }
  }

  override fun <A, B> align(a: K1<LazyList.T, A>, b: K1<LazyList.T, B>): LazyList<Ior<A, B>> = LazyList.narrow(super.align(a, b))

  private fun <A, B, C> alignStep(f: (Ior<A, B>) -> C, x: Cell<A>, y: Cell<B>): Cell<C> = when {
    x is Cell.Cons && y is Cell.Cons -> Cell.Cons(f(Ior.both(x.head, y.head)), alignWith(f, x.tail, y.tail))
    x is Cell.Cons -> Cell.Cons(f(Ior.left(x.head)), x.tail.mapLazy { f(Ior.left(it)) })
    y is Cell.Cons -> Cell.Cons(f(Ior.right(y.head)), y.tail.mapLazy { f(Ior.right(it)) })
    else -> Cell.Nil()
  }
}

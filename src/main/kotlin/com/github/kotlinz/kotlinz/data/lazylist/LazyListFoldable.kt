package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [LazyList]; every fold forces the whole list, so it only terminates for finite lists. */
interface LazyListFoldable: Foldable<LazyList.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<LazyList.T, A>): B = LazyList.narrow(v).asSequence().fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<LazyList.T, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<LazyList.T, A>): List<A> = LazyList.narrow(v).asSequence().toList()
  /** Forces only the first cell. */
  override fun <A> isEmpty(v: K1<LazyList.T, A>): Boolean = LazyList.narrow(v).isEmpty()
}

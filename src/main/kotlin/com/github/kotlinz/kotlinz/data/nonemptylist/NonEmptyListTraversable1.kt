package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 instance for [NonEmptyList]: traverses with an [Apply], running effects left to right. */
interface NonEmptyListTraversable1: Traversable1<NonEmptyList.T>, NonEmptyListTraversable, NonEmptyListFoldable1 {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<NonEmptyList.T, A>): K1<G, K1<NonEmptyList.T, B>> {
    val list = NonEmptyList.narrow(v).toList()
    val last = apply.fmap({ b: B -> NonEmptyList(b, listOf()) as K1<NonEmptyList.T, B> }, f(list.last()))
    return list.dropLast(1).foldRight(last) { a, acc -> apply.ap(apply.fmap(prepend(), f(a)), acc) }
  }

  private fun <B> prepend(): (B) -> (K1<NonEmptyList.T, B>) -> K1<NonEmptyList.T, B> =
    { b -> { bs -> NonEmptyList(b, NonEmptyList.narrow(bs).toList()) } }
}

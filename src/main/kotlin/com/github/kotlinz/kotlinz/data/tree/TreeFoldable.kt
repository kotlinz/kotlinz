package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [Tree], visiting the nodes in pre-order. */
interface TreeFoldable: Foldable<Tree.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<Tree.T, A>): B = toList(v).fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<Tree.T, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<Tree.T, A>): List<A> = Tree.narrow(v).flatten()
  override fun <A> isEmpty(v: K1<Tree.T, A>): Boolean = false
}

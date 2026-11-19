package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [Tree]; pairs children by index and drops the subtrees present in only one tree. */
interface TreeZip: Zip<Tree.T>, TreeSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<Tree.T, A>, b: K1<Tree.T, B>): Tree<C> {
    val left = Tree.narrow(a)
    val right = Tree.narrow(b)
    return Tree(f(left.root, right.root), left.children.zip(right.children) { x, y -> zipWith(f, x, y) })
  }

  override fun <A, B> zip(a: K1<Tree.T, A>, b: K1<Tree.T, B>): Tree<Pair<A, B>> = Tree.narrow(super.zip(a, b))
}

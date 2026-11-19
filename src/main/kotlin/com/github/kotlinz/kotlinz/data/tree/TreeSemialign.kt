package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.alignLists
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [Tree]; pairs children by index and keeps the subtrees present in only one tree. */
interface TreeSemialign: Semialign<Tree.T>, TreeFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<Tree.T, A>, b: K1<Tree.T, B>): Tree<C> {
    val left = Tree.narrow(a)
    val right = Tree.narrow(b)
    return Tree(f(Ior.both(left.root, right.root)), alignLists(left.children, right.children).map { alignChild(f, it) })
  }

  override fun <A, B> align(a: K1<Tree.T, A>, b: K1<Tree.T, B>): Tree<Ior<A, B>> = Tree.narrow(super.align(a, b))

  private fun <A, B, C> alignChild(f: (Ior<A, B>) -> C, child: Ior<Tree<A>, Tree<B>>): Tree<C> = when (child) {
    is Ior.Both -> alignWith(f, child.left, child.right)
    is Ior.Left -> fmap({ x: A -> f(Ior.left(x)) }, child.value)
    is Ior.Right -> fmap({ y: B -> f(Ior.right(y)) }, child.value)
  }
}

package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Tree]; [duplicate] replaces every node with the subtree rooted at it. */
interface TreeComonad: Comonad<Tree.T>, TreeCopointed, TreeFunctor {
  override fun <A> duplicate(v: K1<Tree.T, A>): Tree<K1<Tree.T, A>> {
    val tree = Tree.narrow(v)
    return Tree(tree, tree.children.map { duplicate(it) })
  }

  override fun <A, B> extend(f: (K1<Tree.T, A>) -> B, v: K1<Tree.T, A>): Tree<B> = Tree.narrow(super.extend(f, v))
}

package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Tree]. */
interface TreeFunctor: Functor<Tree.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Tree.T, A>): Tree<B> {
    val tree = Tree.narrow(v)
    return Tree(f(tree.root), tree.children.map { fmap(f, it) })
  }
}

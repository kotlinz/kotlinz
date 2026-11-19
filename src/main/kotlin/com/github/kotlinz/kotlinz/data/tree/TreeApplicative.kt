package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * Applicative instance for [Tree], matching Haskell's `Data.Tree`: the root function is applied to
 * the whole value tree, and every child function subtree is applied recursively.
 */
interface TreeApplicative: Applicative<Tree.T>, TreeFunctor {
  override fun <A> pure(v: A): Tree<A> = Tree.leaf(v)

  override fun <A, B> ap(f: K1<Tree.T, (A) -> B>, v: K1<Tree.T, A>): Tree<B> {
    val fs = Tree.narrow(f)
    val tree = Tree.narrow(v)
    val mapped = tree.children.map { fmap(fs.root, it) }
    return Tree(fs.root(tree.root), mapped + fs.children.map { ap(it, tree) })
  }
}

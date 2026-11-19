package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 instance for [Tree]: traverses with an [Apply], running the effects in pre-order. */
interface TreeTraversable1: Traversable1<Tree.T>, TreeTraversable, TreeFoldable1 {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<Tree.T, A>): K1<G, K1<Tree.T, B>> {
    val tree = Tree.narrow(v)
    val root = f(tree.root)
    val last = tree.children.lastOrNull() ?: return apply.fmap({ b: B -> Tree.leaf(b) }, root)
    val node = apply.fmap({ b: B -> { children: List<Tree<B>> -> Tree(b, children) as K1<Tree.T, B> } }, root)
    return apply.ap(node, traverseChildren1(apply, f, tree.children.dropLast(1), last))
  }

  private fun <G, A, B> traverseChildren1(apply: Apply<G>, f: (A) -> K1<G, B>, init: List<Tree<A>>, last: Tree<A>): K1<G, List<Tree<B>>> {
    val prepend = { t: K1<Tree.T, B> -> { rest: List<Tree<B>> -> listOf(Tree.narrow(t)) + rest } }
    val lastList = apply.fmap({ t: K1<Tree.T, B> -> listOf(Tree.narrow(t)) }, traverse1(apply, f, last))
    return init.foldRight(lastList) { child, acc -> apply.ap(apply.fmap(prepend, traverse1(apply, f, child)), acc) }
  }
}

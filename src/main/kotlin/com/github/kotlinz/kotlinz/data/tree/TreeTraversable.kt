package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [Tree], running the effects in pre-order. */
interface TreeTraversable: Traversable<Tree.T>, TreeFunctor, TreeFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<Tree.T, A>): K1<G, K1<Tree.T, B>> {
    val tree = Tree.narrow(v)
    val node = applicative.fmap({ b: B -> { children: List<Tree<B>> -> Tree(b, children) as K1<Tree.T, B> } }, f(tree.root))
    return applicative.ap(node, traverseChildren(applicative, f, tree.children))
  }

  private fun <G, A, B> traverseChildren(applicative: Applicative<G>, f: (A) -> K1<G, B>, children: List<Tree<A>>): K1<G, List<Tree<B>>> =
    children.foldRight(applicative.pure(listOf())) { child, acc ->
      val prepend = { t: K1<Tree.T, B> -> { rest: List<Tree<B>> -> listOf(Tree.narrow(t)) + rest } }
      applicative.ap(applicative.fmap(prepend, traverse(applicative, f, child)), acc)
    }
}

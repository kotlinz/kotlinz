package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Tree]; [bind] grafts the tree produced for each node, keeping the original children after the new ones. */
interface TreeMonad: Monad<Tree.T>, TreeApplicative {
  override fun <A> join(v: K1<Tree.T, K1<Tree.T, A>>): Tree<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<Tree.T, B>, v: K1<Tree.T, A>): Tree<B> {
    val tree = Tree.narrow(v)
    val grafted = Tree.narrow(f(tree.root))
    return Tree(grafted.root, grafted.children + tree.children.map { bind(f, it) })
  }
}

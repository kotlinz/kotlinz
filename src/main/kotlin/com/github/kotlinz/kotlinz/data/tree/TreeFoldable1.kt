package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [Tree], reducing the nodes in pre-order. */
interface TreeFoldable1: Foldable1<Tree.T>, TreeFoldable {
  override fun <A> head(v: K1<Tree.T, A>): A = Tree.narrow(v).root
}

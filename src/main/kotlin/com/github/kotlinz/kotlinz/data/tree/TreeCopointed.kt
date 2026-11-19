package com.github.kotlinz.kotlinz.data.tree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Tree]; extracts the root. */
interface TreeCopointed: Copointed<Tree.T> {
  override fun <A> extract(v: K1<Tree.T, A>): A = Tree.narrow(v).root
}

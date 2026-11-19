package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.IxMonad

/** IxMonad instance for [IndexedState]. Deeply nested `ibind` chains are not stack-safe. */
interface IndexedStateIxMonad: IxMonad<IndexedState.T>, IndexedStateIxApplicative {
  override fun <I, J, K, A, B> ibind(f: (A) -> K3<IndexedState.T, J, K, B>, v: K3<IndexedState.T, I, J, A>): IndexedState<I, K, B> {
    val state = IndexedState.narrow(v)
    return IndexedState { i -> state.run(i).let { (a, j) -> IndexedState.narrow(f(a)).run(j) } }
  }
}

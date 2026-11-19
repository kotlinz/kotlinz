package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.IxFunctor

/** IxFunctor instance for [IndexedState]. */
interface IndexedStateIxFunctor: IxFunctor<IndexedState.T> {
  override fun <I, J, A, B> imap(f: (A) -> B, v: K3<IndexedState.T, I, J, A>): IndexedState<I, J, B> {
    val state = IndexedState.narrow(v)
    return IndexedState { i -> state.run(i).let { (a, j) -> Pair(f(a), j) } }
  }
}

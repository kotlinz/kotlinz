package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.IxApplicative

/** IxApplicative instance for [IndexedState]; runs the function computation first. */
interface IndexedStateIxApplicative: IxApplicative<IndexedState.T>, IndexedStateIxFunctor {
  override fun <I, A> ipure(v: A): IndexedState<I, I, A> = IndexedState.pure(v)

  override fun <I, J, K, A, B> iap(f: K3<IndexedState.T, I, J, (A) -> B>, v: K3<IndexedState.T, J, K, A>): IndexedState<I, K, B> {
    val (state, statef) = Pair(IndexedState.narrow(v), IndexedState.narrow(f))
    return IndexedState { i -> statef.run(i).let { (g, j) -> state.run(j).let { (a, k) -> Pair(g(a), k) } } }
  }
}

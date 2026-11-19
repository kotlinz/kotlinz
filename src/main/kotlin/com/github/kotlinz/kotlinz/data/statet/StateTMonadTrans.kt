package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [StateT]: lifted computations leave the state unchanged. */
interface StateTMonadTrans<F, S> : MonadTrans<F, K2<StateT.T, F, S>>, StateTMonad<F, S> {
    override fun <A> lift(v: K1<F, A>): StateT<F, S, A> = StateT(monad) { s -> monad.fmap({ a -> Pair(a, s) }, v) }
}

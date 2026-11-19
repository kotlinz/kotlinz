package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [ReaderT]: lifted computations ignore the environment. */
interface ReaderTMonadTrans<F, R> : MonadTrans<F, K2<ReaderT.T, F, R>>, ReaderTMonad<F, R> {
    override fun <A> lift(v: K1<F, A>): ReaderT<F, R, A> = ReaderT(monad) { _ -> v }
}

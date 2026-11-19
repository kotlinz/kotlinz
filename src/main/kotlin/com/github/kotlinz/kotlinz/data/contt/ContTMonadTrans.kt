package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [ContT]: a lifted computation feeds its result to the continuation via `bind`. */
interface ContTMonadTrans<R, F> : MonadTrans<F, K2<ContT.T, R, F>>, ContTMonad<R, F> {
    override fun <A> lift(v: K1<F, A>): ContT<R, F, A> = ContT(monad) { k -> monad.bind(k, v) }
}

package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [LogicT]: a lifted computation yields its single result. */
interface LogicTMonadTrans<F> : MonadTrans<F, K1<LogicT.T, F>>, LogicTMonad<F> {
    override fun <A> lift(v: K1<F, A>): LogicT<F, A> = object : LogicT<F, A>(monad) {
        override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            monad.bind({ a -> success(a, failure) }, v)
    }
}

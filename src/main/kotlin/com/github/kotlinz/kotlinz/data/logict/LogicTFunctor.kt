package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [LogicT]; requires a `Monad<F>` for the underlying effect. */
interface LogicTFunctor<F> : Functor<K1<LogicT.T, F>> {
    /** The underlying monad. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<LogicT.T, F>, A>): LogicT<F, B> = object : LogicT<F, B>(monad) {
        override fun <R> run(success: (B, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            LogicT.narrow(v).run({ a, next -> success(f(a), next) }, failure)
    }
}

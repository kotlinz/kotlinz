package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [LogicT]; `bind` explores results depth-first (see [LogicTMonadLogic.fairBind] for a fair variant). */
interface LogicTMonad<F> : Monad<K1<LogicT.T, F>>, LogicTApplicative<F>, LogicTFunctor<F> {
    override fun <A> join(v: K1<K1<LogicT.T, F>, K1<K1<LogicT.T, F>, A>>): LogicT<F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K1<LogicT.T, F>, B>, v: K1<K1<LogicT.T, F>, A>): LogicT<F, B> = object : LogicT<F, B>(monad) {
        override fun <R> run(success: (B, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            LogicT.narrow(v).run({ a, next -> LogicT.narrow(f(a)).run(success, next) }, failure)
    }
}

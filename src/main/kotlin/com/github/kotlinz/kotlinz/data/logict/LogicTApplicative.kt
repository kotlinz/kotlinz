package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [LogicT]; applies every function result to every argument result, in order. */
interface LogicTApplicative<F> : Applicative<K1<LogicT.T, F>>, LogicTFunctor<F> {
    override fun <A> pure(v: A): LogicT<F, A> = object : LogicT<F, A>(monad) {
        override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            success(v, failure)
    }

    override fun <A, B> ap(f: K1<K1<LogicT.T, F>, (A) -> B>, v: K1<K1<LogicT.T, F>, A>): LogicT<F, B> = object : LogicT<F, B>(monad) {
        override fun <R> run(success: (B, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            LogicT.narrow(f).run({ g, next -> LogicT.narrow(v).run({ a, rest -> success(g(a), rest) }, next) }, failure)
    }
}

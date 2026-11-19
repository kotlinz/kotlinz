package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/** Alternative instance for [LogicT]: [empty] has no results and [orElse] yields the results of `v1`, then `v2`. */
interface LogicTAlternative<F> : Alternative<K1<LogicT.T, F>>, LogicTApplicative<F> {
    override fun <A> empty(): LogicT<F, A> = object : LogicT<F, A>(monad) {
        override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> = failure()
    }

    override fun <A> orElse(v1: K1<K1<LogicT.T, F>, A>, v2: K1<K1<LogicT.T, F>, A>): LogicT<F, A> = object : LogicT<F, A>(monad) {
        override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
            LogicT.narrow(v1).run(success) { LogicT.narrow(v2).run(success, failure) }
    }
}

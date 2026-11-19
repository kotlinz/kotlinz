package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.Monad

internal typealias Step<F, A> = Maybe<Pair<A, LogicT<F, A>>>

internal fun <F, A> deferLogic(monad: Monad<F>, f: () -> LogicT<F, A>): LogicT<F, A> = object : LogicT<F, A>(monad) {
    override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
        f().run(success, failure)
}

internal fun <F, A> splitStep(v: LogicT<F, A>): K1<F, Step<F, A>> {
    val m = v.monad
    return v.run({ a, next -> m.pure(Maybe.Just(Pair(a, reflect(m, next)))) }, { m.pure(Maybe.None()) })
}

internal fun <F, A> firstResult(v: LogicT<F, A>): K1<F, Maybe<A>> =
    v.run({ a, _ -> v.monad.pure(Maybe.Just(a)) }, { v.monad.pure(Maybe.None()) })

internal fun <F, A> collectAll(v: LogicT<F, A>): K1<F, List<A>> =
    v.run({ a, next -> v.monad.fmap({ rest: List<A> -> listOf(a) + rest }, next()) }, { v.monad.pure(emptyList()) })

internal fun <F, A> collectMany(n: Int, v: LogicT<F, A>): K1<F, List<A>> {
    if (n <= 0) return v.monad.pure(emptyList())
    return v.monad.bind({ step: Step<F, A> -> prependStep(n, v.monad, step) }, splitStep(v))
}

private fun <F, A> prependStep(n: Int, m: Monad<F>, step: Step<F, A>): K1<F, List<A>> = when (step) {
    is Maybe.Just -> m.fmap({ rest: List<A> -> listOf(step.value.first) + rest }, collectMany(n - 1, step.value.second))
    is Maybe.None -> m.pure(emptyList())
}

private fun <F, A> reflect(m: Monad<F>, next: () -> K1<F, Step<F, A>>): LogicT<F, A> = object : LogicT<F, A>(m) {
    override fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R> =
        m.bind({ step: Step<F, A> -> fromStep(m, step).run(success, failure) }, next())
}

private fun <F, A> fromStep(m: Monad<F>, step: Step<F, A>): LogicT<F, A> = when (step) {
    is Maybe.Just -> LogicT.pure(m, step.value.first) orElse step.value.second
    is Maybe.None -> LogicT.empty(m)
}

package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [StateT], lifted from a base monad that has MonadReader. */
interface StateTMonadReader<F, S, R> : MonadReader<K2<StateT.T, F, S>, R>, StateTMonadTrans<F, S> {
    /** The underlying monad with environment `R`. */
    override val monad: MonadReader<F, R>

    override fun ask(): StateT<F, S, R> = lift(monad.ask())

    override fun <A> local(f: (R) -> R, v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, A> {
        val state = StateT.narrow(v)
        return StateT(monad) { s -> monad.local(f, state.run(s)) }
    }
}

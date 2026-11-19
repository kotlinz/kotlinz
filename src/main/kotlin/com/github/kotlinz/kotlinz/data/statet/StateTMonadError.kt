package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [StateT], lifted from a base monad that has MonadError; recovery restarts from the input state. */
interface StateTMonadError<F, S, E> : MonadError<K2<StateT.T, F, S>, E>, StateTMonadTrans<F, S> {
    /** The underlying monad with error `E`. */
    override val monad: MonadError<F, E>

    override fun <A> raiseError(e: E): StateT<F, S, A> = lift(monad.raiseError(e))

    override fun <A> handleErrorWith(f: (E) -> K1<K2<StateT.T, F, S>, A>, v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, A> {
        val state = StateT.narrow(v)
        return StateT(monad) { s -> monad.handleErrorWith({ e -> StateT.narrow(f(e)).run(s) }, state.run(s)) }
    }
}

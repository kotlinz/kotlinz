package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [EitherT], lifted from a base monad that has MonadState. */
interface EitherTMonadState<F, E, S> : MonadState<K2<EitherT.T, F, E>, S>, EitherTMonadTrans<F, E> {
    /** The underlying monad with state `S`. */
    override val monad: MonadState<F, S>

    override fun get(): EitherT<F, E, S> = lift(monad.get())
    override fun put(s: S): EitherT<F, E, Unit> = lift(monad.put(s))
    override fun <A> state(f: (S) -> Pair<A, S>): EitherT<F, E, A> = lift(monad.state(f))
}

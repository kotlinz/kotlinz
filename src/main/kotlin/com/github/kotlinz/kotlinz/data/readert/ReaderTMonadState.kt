package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [ReaderT], lifted from a base monad that has MonadState. */
interface ReaderTMonadState<F, R, S> : MonadState<K2<ReaderT.T, F, R>, S>, ReaderTMonadTrans<F, R> {
    /** The underlying monad with state `S`. */
    override val monad: MonadState<F, S>

    override fun get(): ReaderT<F, R, S> = lift(monad.get())
    override fun put(s: S): ReaderT<F, R, Unit> = lift(monad.put(s))
    override fun <A> state(f: (S) -> Pair<A, S>): ReaderT<F, R, A> = lift(monad.state(f))
}

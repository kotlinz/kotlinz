package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [WriterT], lifted from a base monad that has MonadState. */
interface WriterTMonadState<F, W: Monoid<*, W>, S> : MonadState<K2<WriterT.T, F, W>, S>, WriterTMonadTrans<F, W> {
    /** The underlying monad with state `S`. */
    override val monad: MonadState<F, S>

    override fun get(): WriterT<F, W, S> = lift(monad.get())
    override fun put(s: S): WriterT<F, W, Unit> = lift(monad.put(s))
    override fun <A> state(f: (S) -> Pair<A, S>): WriterT<F, W, A> = lift(monad.state(f))
}

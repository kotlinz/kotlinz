package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [MaybeT], lifted from a base monad that has MonadState. */
interface MaybeTMonadState<F, S> : MonadState<K1<MaybeT.T, F>, S>, MaybeTMonadTrans<F> {
    /** The underlying monad with state `S`. */
    override val monad: MonadState<F, S>

    override fun get(): MaybeT<F, S> = lift(monad.get())
    override fun put(s: S): MaybeT<F, Unit> = lift(monad.put(s))
    override fun <A> state(f: (S) -> Pair<A, S>): MaybeT<F, A> = lift(monad.state(f))
}

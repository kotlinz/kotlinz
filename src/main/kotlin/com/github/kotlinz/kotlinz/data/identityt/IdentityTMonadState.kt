package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [IdentityT], lifted from a base monad that has MonadState. */
interface IdentityTMonadState<F, S> : MonadState<K1<IdentityT.T, F>, S>, IdentityTMonadTrans<F> {
    /** The underlying monad with state `S`. */
    override val monad: MonadState<F, S>

    override fun get(): IdentityT<F, S> = lift(monad.get())
    override fun put(s: S): IdentityT<F, Unit> = lift(monad.put(s))
    override fun <A> state(f: (S) -> Pair<A, S>): IdentityT<F, A> = lift(monad.state(f))
}

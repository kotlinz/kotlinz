package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadState

interface StateTMonadState<F, S> : MonadState<K2<StateT.T, F, S>, S>, StateTMonad<F, S> {
    override fun get(): StateT<F, S, S> = state { s -> Pair(s, s) }
    override fun put(s: S): StateT<F, S, Unit> = state { Pair(Unit, s) }
    override fun modify(f: (S) -> S): StateT<F, S, Unit> = state { s -> Pair(Unit, f(s)) }
    override fun <A> state(f: (S) -> Pair<A, S>): StateT<F, S, A> = StateT(monad) { s -> monad.pure(f(s)) }
}

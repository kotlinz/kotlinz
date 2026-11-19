package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [RWST]. */
interface RWSTMonadState<F, R, W: Monoid<*, W>, S> : MonadState<K4<RWST.T, F, R, W, S>, S>, RWSTMonad<F, R, W, S> {
    override fun get(): RWST<F, R, W, S, S> = state { s -> Pair(s, s) }
    override fun put(s: S): RWST<F, R, W, S, Unit> = state { Pair(Unit, s) }
    override fun modify(f: (S) -> S): RWST<F, R, W, S, Unit> = state { s -> Pair(Unit, f(s)) }

    override fun <A> state(f: (S) -> Pair<A, S>): RWST<F, R, W, S, A> {
        return RWST(monad, type) { _, s -> f(s).let { (a, s2) -> monad.pure(Triple(a, s2, Monoid.mzero(type))) } }
    }
}

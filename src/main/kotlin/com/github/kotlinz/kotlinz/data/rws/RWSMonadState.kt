package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadState

interface RWSMonadState<R, W: Monoid<*, W>, S>: MonadState<K3<RWS.T, R, W, S>, S>, RWSMonad<R, W, S> {
  override fun get(): RWS<R, W, S, S> = state { s -> Pair(s, s) }
  override fun put(s: S): RWS<R, W, S, Unit> = state { Pair(Unit, s) }
  override fun modify(f: (S) -> S): RWS<R, W, S, Unit> = state { s -> Pair(Unit, f(s)) }

  override fun <A> state(f: (S) -> Pair<A, S>): RWS<R, W, S, A> {
    return RWS(type) { _, s -> f(s).let { (a, s2) -> Triple(a, s2, Monoid.mzero(type)) } }
  }
}

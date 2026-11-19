package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** MonadFix instance for [Maybe]: when `f` yields [Maybe.None] the recursive value never becomes available. */
interface MaybeMonadFix: MonadFix<Maybe.T>, MaybeMonad {
  override fun <A> mfix(f: (() -> A) -> K1<Maybe.T, A>): Maybe<A> {
    val knot = Knot<A>()
    return Maybe.narrow(f(knot.thunk)).also { if (it is Maybe.Just) knot.tie(it.value) }
  }
}

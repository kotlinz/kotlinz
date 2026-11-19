package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** MonadFix instance for [Function0]: ties the knot anew on every run. */
interface Function0MonadFix: MonadFix<Function0.T>, Function0Monad {
  override fun <A> mfix(f: (() -> A) -> K1<Function0.T, A>): Function0<A> =
    Function0 { Knot.fix { x -> Function0.narrow(f(x)).run() } }
}

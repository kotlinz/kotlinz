package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** MonadFix instance for [Identity]: the plain lazy fixed point of the wrapped function. */
interface IdentityMonadFix: MonadFix<Identity.T>, IdentityMonad {
  override fun <A> mfix(f: (() -> A) -> K1<Identity.T, A>): Identity<A> =
    Identity(Knot.fix { x -> Identity.narrow(f(x)).value })
}

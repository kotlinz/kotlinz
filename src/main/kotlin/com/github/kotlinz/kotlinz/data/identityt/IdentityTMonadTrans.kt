package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [IdentityT]: lifting just wraps the base computation. */
interface IdentityTMonadTrans<F> : MonadTrans<F, K1<IdentityT.T, F>>, IdentityTMonad<F> {
    override fun <A> lift(v: K1<F, A>): IdentityT<F, A> = IdentityT(monad, v)
}

package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [IdentityT], delegating to the underlying monad. */
interface IdentityTFunctor<F> : Functor<K1<IdentityT.T, F>> {
    /** The underlying monad. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, B> {
        return IdentityT(monad, monad.fmap(f, IdentityT.narrow(v).run))
    }
}

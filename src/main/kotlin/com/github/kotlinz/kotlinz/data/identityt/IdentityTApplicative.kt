package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [IdentityT], delegating to the underlying monad. */
interface IdentityTApplicative<F> : Applicative<K1<IdentityT.T, F>>, IdentityTFunctor<F> {
    override fun <A> pure(v: A): IdentityT<F, A> = IdentityT(monad, monad.pure(v))

    override fun <A, B> ap(f: K1<K1<IdentityT.T, F>, (A) -> B>, v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, B> {
        return IdentityT(monad, monad.ap(IdentityT.narrow(f).run, IdentityT.narrow(v).run))
    }
}

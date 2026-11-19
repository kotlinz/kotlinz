package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [IdentityT], delegating to the underlying monad. */
interface IdentityTMonad<F> : Monad<K1<IdentityT.T, F>>, IdentityTApplicative<F>, IdentityTFunctor<F> {
    override fun <A> join(v: K1<K1<IdentityT.T, F>, K1<K1<IdentityT.T, F>, A>>): IdentityT<F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K1<IdentityT.T, F>, B>, v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, B> {
        return IdentityT(monad, monad.bind({ a: A -> IdentityT.narrow(f(a)).run }, IdentityT.narrow(v).run))
    }
}

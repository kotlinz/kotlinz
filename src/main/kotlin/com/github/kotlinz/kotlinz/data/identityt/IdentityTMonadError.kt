package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [IdentityT], lifted from a base monad that has MonadError. */
interface IdentityTMonadError<F, E> : MonadError<K1<IdentityT.T, F>, E>, IdentityTMonadTrans<F> {
    /** The underlying monad with error `E`. */
    override val monad: MonadError<F, E>

    override fun <A> raiseError(e: E): IdentityT<F, A> = lift(monad.raiseError(e))

    override fun <A> handleErrorWith(f: (E) -> K1<K1<IdentityT.T, F>, A>, v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, A> {
        return IdentityT(monad, monad.handleErrorWith({ e -> IdentityT.narrow(f(e)).run }, IdentityT.narrow(v).run))
    }
}

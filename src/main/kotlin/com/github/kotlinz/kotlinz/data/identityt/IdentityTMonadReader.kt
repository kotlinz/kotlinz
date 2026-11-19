package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [IdentityT], lifted from a base monad that has MonadReader. */
interface IdentityTMonadReader<F, R> : MonadReader<K1<IdentityT.T, F>, R>, IdentityTMonadTrans<F> {
    /** The underlying monad with environment `R`. */
    override val monad: MonadReader<F, R>

    override fun ask(): IdentityT<F, R> = lift(monad.ask())

    override fun <A> local(f: (R) -> R, v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, A> {
        return IdentityT(monad, monad.local(f, IdentityT.narrow(v).run))
    }
}

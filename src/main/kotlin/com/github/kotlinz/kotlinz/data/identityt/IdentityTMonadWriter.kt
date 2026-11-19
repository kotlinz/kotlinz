package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [IdentityT], lifted from a base monad that has MonadWriter. */
interface IdentityTMonadWriter<F, W> : MonadWriter<K1<IdentityT.T, F>, W>, IdentityTMonadTrans<F> {
    /** The underlying monad with output `W`. */
    override val monad: MonadWriter<F, W>

    override fun <A> writer(v: Pair<A, W>): IdentityT<F, A> = lift(monad.writer(v))

    override fun <A> listen(v: K1<K1<IdentityT.T, F>, A>): IdentityT<F, Pair<A, W>> {
        return IdentityT(monad, monad.listen(IdentityT.narrow(v).run))
    }

    override fun <A> pass(v: K1<K1<IdentityT.T, F>, Pair<A, (W) -> W>>): IdentityT<F, A> {
        return IdentityT(monad, monad.pass(IdentityT.narrow(v).run))
    }
}

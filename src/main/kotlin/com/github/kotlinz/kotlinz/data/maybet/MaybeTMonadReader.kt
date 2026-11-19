package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [MaybeT], lifted from a base monad that has MonadReader. */
interface MaybeTMonadReader<F, R> : MonadReader<K1<MaybeT.T, F>, R>, MaybeTMonadTrans<F> {
    /** The underlying monad with environment `R`. */
    override val monad: MonadReader<F, R>

    override fun ask(): MaybeT<F, R> = lift(monad.ask())

    override fun <A> local(f: (R) -> R, v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, A> {
        return MaybeT(monad, monad.local(f, MaybeT.narrow(v).run))
    }
}

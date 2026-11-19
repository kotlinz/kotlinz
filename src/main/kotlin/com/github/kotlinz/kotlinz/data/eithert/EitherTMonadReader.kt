package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [EitherT], lifted from a base monad that has MonadReader. */
interface EitherTMonadReader<F, E, R> : MonadReader<K2<EitherT.T, F, E>, R>, EitherTMonadTrans<F, E> {
    /** The underlying monad with environment `R`. */
    override val monad: MonadReader<F, R>

    override fun ask(): EitherT<F, E, R> = lift(monad.ask())

    override fun <A> local(f: (R) -> R, v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, A> {
        return EitherT(monad, monad.local(f, EitherT.narrow(v).run))
    }
}

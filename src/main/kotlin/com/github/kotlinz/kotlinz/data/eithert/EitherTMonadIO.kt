package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [EitherT] over a base monad that has MonadIO. */
interface EitherTMonadIO<F, E> : MonadIO<K2<EitherT.T, F, E>>, EitherTMonadTrans<F, E> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): EitherT<F, E, A> = lift(monad.liftIO(v))
}

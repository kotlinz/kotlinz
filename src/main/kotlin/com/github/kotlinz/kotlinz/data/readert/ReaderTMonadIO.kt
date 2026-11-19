package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [ReaderT] over a base monad that has MonadIO. */
interface ReaderTMonadIO<F, R> : MonadIO<K2<ReaderT.T, F, R>>, ReaderTMonadTrans<F, R> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): ReaderT<F, R, A> = lift(monad.liftIO(v))
}

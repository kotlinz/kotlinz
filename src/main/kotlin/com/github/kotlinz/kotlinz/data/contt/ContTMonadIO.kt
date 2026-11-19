package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [ContT] over a base monad that has MonadIO. */
interface ContTMonadIO<R, F> : MonadIO<K2<ContT.T, R, F>>, ContTMonadTrans<R, F> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): ContT<R, F, A> = lift(monad.liftIO(v))
}

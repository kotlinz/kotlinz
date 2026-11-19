package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [ListT] over a base monad that has MonadIO. */
interface ListTMonadIO<F> : MonadIO<K1<ListT.T, F>>, ListTMonadTrans<F> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): ListT<F, A> = lift(monad.liftIO(v))
}

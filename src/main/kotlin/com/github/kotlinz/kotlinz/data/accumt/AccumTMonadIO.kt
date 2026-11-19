package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [AccumT] over a base monad that has MonadIO. */
interface AccumTMonadIO<F, W: Monoid<*, W>> : MonadIO<K2<AccumT.T, F, W>>, AccumTMonadTrans<F, W> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): AccumT<F, W, A> = lift(monad.liftIO(v))
}

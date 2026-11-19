package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [WriterT] over a base monad that has MonadIO. */
interface WriterTMonadIO<F, W: Monoid<*, W>> : MonadIO<K2<WriterT.T, F, W>>, WriterTMonadTrans<F, W> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): WriterT<F, W, A> = lift(monad.liftIO(v))
}

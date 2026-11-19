package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [LogicT] over a base monad that has MonadIO. */
interface LogicTMonadIO<F> : MonadIO<K1<LogicT.T, F>>, LogicTMonadTrans<F> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): LogicT<F, A> = lift(monad.liftIO(v))
}

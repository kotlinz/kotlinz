package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [StateT] over a base monad that has MonadIO. */
interface StateTMonadIO<F, S> : MonadIO<K2<StateT.T, F, S>>, StateTMonadTrans<F, S> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): StateT<F, S, A> = lift(monad.liftIO(v))
}

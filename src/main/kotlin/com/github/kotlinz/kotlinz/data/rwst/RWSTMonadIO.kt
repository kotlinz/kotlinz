package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [RWST] over a base monad that has MonadIO. */
interface RWSTMonadIO<F, R, W: Monoid<*, W>, S> : MonadIO<K4<RWST.T, F, R, W, S>>, RWSTMonadTrans<F, R, W, S> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): RWST<F, R, W, S, A> = lift(monad.liftIO(v))
}

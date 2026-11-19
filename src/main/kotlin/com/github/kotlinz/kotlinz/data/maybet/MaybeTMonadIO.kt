package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [MaybeT] over a base monad that has MonadIO. */
interface MaybeTMonadIO<F> : MonadIO<K1<MaybeT.T, F>>, MaybeTMonadTrans<F> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): MaybeT<F, A> = lift(monad.liftIO(v))
}

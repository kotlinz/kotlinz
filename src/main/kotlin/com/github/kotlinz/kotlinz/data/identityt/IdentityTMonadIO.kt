package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [IdentityT] over a base monad that has MonadIO. */
interface IdentityTMonadIO<F> : MonadIO<K1<IdentityT.T, F>>, IdentityTMonadTrans<F> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <A> liftIO(v: IO<A>): IdentityT<F, A> = lift(monad.liftIO(v))
}

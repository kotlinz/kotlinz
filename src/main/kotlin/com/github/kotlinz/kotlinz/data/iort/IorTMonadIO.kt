package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [IorT] over a base monad that has MonadIO. */
interface IorTMonadIO<F, A: Semigroup<*, A>> : MonadIO<K2<IorT.T, F, A>>, IorTMonadTrans<F, A> {
    /** The underlying monad, able to lift IO. */
    override val monad: MonadIO<F>

    override fun <B> liftIO(v: IO<B>): IorT<F, A, B> = lift(monad.liftIO(v))
}

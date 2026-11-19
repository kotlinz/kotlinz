package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [WriterT], lifted from a base monad that has MonadReader. */
interface WriterTMonadReader<F, W: Monoid<*, W>, R> : MonadReader<K2<WriterT.T, F, W>, R>, WriterTMonadTrans<F, W> {
    /** The underlying monad with environment `R`. */
    override val monad: MonadReader<F, R>

    override fun ask(): WriterT<F, W, R> = lift(monad.ask())

    override fun <A> local(f: (R) -> R, v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, A> {
        return WriterT(monad, type, monad.local(f, WriterT.narrow(v).run))
    }
}

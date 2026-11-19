package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [WriterT]: lifted computations log nothing. */
interface WriterTMonadTrans<F, W: Monoid<*, W>> : MonadTrans<F, K2<WriterT.T, F, W>>, WriterTMonad<F, W> {
    override fun <A> lift(v: K1<F, A>): WriterT<F, W, A> {
        return WriterT(monad, type, monad.fmap({ a -> Pair(a, Monoid.mzero(type)) }, v))
    }
}

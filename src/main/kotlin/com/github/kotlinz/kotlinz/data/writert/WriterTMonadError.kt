package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [WriterT], lifted from a base monad that has MonadError; output of a failed branch is lost. */
interface WriterTMonadError<F, W: Monoid<*, W>, E> : MonadError<K2<WriterT.T, F, W>, E>, WriterTMonadTrans<F, W> {
    /** The underlying monad with error `E`. */
    override val monad: MonadError<F, E>

    override fun <A> raiseError(e: E): WriterT<F, W, A> = lift(monad.raiseError(e))

    override fun <A> handleErrorWith(f: (E) -> K1<K2<WriterT.T, F, W>, A>, v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, A> {
        return WriterT(monad, type, monad.handleErrorWith({ e -> WriterT.narrow(f(e)).run }, WriterT.narrow(v).run))
    }
}

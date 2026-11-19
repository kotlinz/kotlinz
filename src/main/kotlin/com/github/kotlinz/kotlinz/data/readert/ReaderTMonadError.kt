package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [ReaderT], lifted from a base monad that has MonadError. */
interface ReaderTMonadError<F, R, E> : MonadError<K2<ReaderT.T, F, R>, E>, ReaderTMonadTrans<F, R> {
    /** The underlying monad with error `E`. */
    override val monad: MonadError<F, E>

    override fun <A> raiseError(e: E): ReaderT<F, R, A> = lift(monad.raiseError(e))

    override fun <A> handleErrorWith(f: (E) -> K1<K2<ReaderT.T, F, R>, A>, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, A> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> monad.handleErrorWith({ e -> ReaderT.narrow(f(e)).run(r) }, reader.run(r)) }
    }
}

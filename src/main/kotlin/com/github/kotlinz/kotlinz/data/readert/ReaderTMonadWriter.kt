package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [ReaderT], lifted from a base monad that has MonadWriter. */
interface ReaderTMonadWriter<F, R, W> : MonadWriter<K2<ReaderT.T, F, R>, W>, ReaderTMonadTrans<F, R> {
    /** The underlying monad with output `W`. */
    override val monad: MonadWriter<F, W>

    override fun <A> writer(v: Pair<A, W>): ReaderT<F, R, A> = lift(monad.writer(v))

    override fun <A> listen(v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, Pair<A, W>> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> monad.listen(reader.run(r)) }
    }

    override fun <A> pass(v: K1<K2<ReaderT.T, F, R>, Pair<A, (W) -> W>>): ReaderT<F, R, A> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> monad.pass(reader.run(r)) }
    }
}

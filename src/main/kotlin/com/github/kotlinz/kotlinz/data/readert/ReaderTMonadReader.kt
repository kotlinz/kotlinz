package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [ReaderT]. */
interface ReaderTMonadReader<F, R> : MonadReader<K2<ReaderT.T, F, R>, R>, ReaderTMonad<F, R> {
    override fun ask(): ReaderT<F, R, R> = ReaderT(monad) { r -> monad.pure(r) }

    override fun <A> local(f: (R) -> R, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, A> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> reader.run(f(r)) }
    }
}

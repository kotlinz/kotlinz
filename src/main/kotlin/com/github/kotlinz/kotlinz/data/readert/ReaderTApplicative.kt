package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [ReaderT], delegating to the underlying monad. */
interface ReaderTApplicative<F, R> : Applicative<K2<ReaderT.T, F, R>>, ReaderTFunctor<F, R> {
    override fun <A> pure(v: A): ReaderT<F, R, A> = ReaderT(monad) { _ -> monad.pure(v) }

    override fun <A, B> ap(f: K1<K2<ReaderT.T, F, R>, (A) -> B>, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, B> {
        val reader = ReaderT.narrow(v)
        val readerf = ReaderT.narrow(f)
        return ReaderT(monad) { r -> monad.ap(readerf.run(r), reader.run(r)) }
    }
}

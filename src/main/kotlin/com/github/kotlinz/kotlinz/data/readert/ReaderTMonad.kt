package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [ReaderT], delegating to the underlying monad. */
interface ReaderTMonad<F, R> : Monad<K2<ReaderT.T, F, R>>, ReaderTApplicative<F, R>, ReaderTFunctor<F, R> {
    override fun <A> join(v: K1<K2<ReaderT.T, F, R>, K1<K2<ReaderT.T, F, R>, A>>): ReaderT<F, R, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<ReaderT.T, F, R>, B>, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, B> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> monad.bind({ a: A -> ReaderT.narrow(f(a)).run(r) }, reader.run(r)) }
    }
}

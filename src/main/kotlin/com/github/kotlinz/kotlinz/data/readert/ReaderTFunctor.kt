package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

interface ReaderTFunctor<F, R> : Functor<K2<ReaderT.T, F, R>> {
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<F, R, B> {
        val reader = ReaderT.narrow(v)
        return ReaderT(monad) { r -> monad.fmap(f, reader.run(r)) }
    }
}

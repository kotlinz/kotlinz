package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [ReaderT], moving it from base `F` to base `G`. */
interface ReaderTMFunctor<F, G, R> : MFunctor<F, G, K2<ReaderT.T, F, R>, K2<ReaderT.T, G, R>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<ReaderT.T, F, R>, A>): ReaderT<G, R, A> {
        val reader = ReaderT.narrow(v)
        return ReaderT(target) { r -> nt(reader.run(r)) }
    }
}

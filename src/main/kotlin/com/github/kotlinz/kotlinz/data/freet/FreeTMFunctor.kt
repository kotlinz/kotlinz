package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [FreeT], moving it from inner monad `M` to `N` at every layer. */
interface FreeTMFunctor<F, M, N> : MFunctor<M, N, K2<FreeT.T, F, M>, K2<FreeT.T, F, N>> {
    /** The monad of the target base `N`. */
    val target: Monad<N>

    override fun <A> hoist(nt: NaturalTransformation<M, N>, v: K1<K2<FreeT.T, F, M>, A>): FreeT<F, N, A> {
        val freeT = FreeT.narrow(v)
        val step = { s: FreeF<F, A, FreeT<F, M, A>> -> s.mapNext(freeT.functor) { next -> hoist(nt, next) } }
        return FreeT(freeT.functor, target, nt(freeT.monad.fmap(step, freeT.run)))
    }
}

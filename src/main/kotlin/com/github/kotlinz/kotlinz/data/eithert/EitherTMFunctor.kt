package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [EitherT], moving it from base `F` to base `G`. */
interface EitherTMFunctor<F, G, E> : MFunctor<F, G, K2<EitherT.T, F, E>, K2<EitherT.T, G, E>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<EitherT.T, F, E>, A>): EitherT<G, E, A> {
        return EitherT(target, nt(EitherT.narrow(v).run))
    }
}

package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [MaybeT], moving it from base `F` to base `G`. */
interface MaybeTMFunctor<F, G> : MFunctor<F, G, K1<MaybeT.T, F>, K1<MaybeT.T, G>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K1<MaybeT.T, F>, A>): MaybeT<G, A> {
        return MaybeT(target, nt(MaybeT.narrow(v).run))
    }
}

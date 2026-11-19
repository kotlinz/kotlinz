package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [IdentityT], moving it from base `F` to base `G`. */
interface IdentityTMFunctor<F, G> : MFunctor<F, G, K1<IdentityT.T, F>, K1<IdentityT.T, G>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K1<IdentityT.T, F>, A>): IdentityT<G, A> {
        return IdentityT(target, nt(IdentityT.narrow(v).run))
    }
}

package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [IorT], moving it from base `F` to base `G`. */
interface IorTMFunctor<F, G, A: Semigroup<*, A>> : MFunctor<F, G, K2<IorT.T, F, A>, K2<IorT.T, G, A>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>
    /** The Semigroup class used to combine left values. */
    val type: Class<A>

    override fun <B> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<IorT.T, F, A>, B>): IorT<G, A, B> {
        return IorT(target, type, nt(IorT.narrow(v).run))
    }
}

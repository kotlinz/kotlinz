package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [AccumT], moving it from base `F` to base `G`. */
interface AccumTMFunctor<F, G, W: Monoid<*, W>> : MFunctor<F, G, K2<AccumT.T, F, W>, K2<AccumT.T, G, W>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>
    /** The Monoid class of the accumulated output. */
    val type: Class<W>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<AccumT.T, F, W>, A>): AccumT<G, W, A> {
        val accum = AccumT.narrow(v)
        return AccumT(target, type) { w -> nt(accum.run(w)) }
    }
}

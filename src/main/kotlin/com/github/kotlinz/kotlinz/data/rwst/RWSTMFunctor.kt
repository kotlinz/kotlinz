package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [RWST], moving it from base `F` to base `G`. */
interface RWSTMFunctor<F, G, R, W: Monoid<*, W>, S> : MFunctor<F, G, K4<RWST.T, F, R, W, S>, K4<RWST.T, G, R, W, S>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>
    /** The Monoid class of the log. */
    val type: Class<W>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<G, R, W, S, A> {
        val rwst = RWST.narrow(v)
        return RWST(target, type) { r, s -> nt(rwst.run(r, s)) }
    }
}

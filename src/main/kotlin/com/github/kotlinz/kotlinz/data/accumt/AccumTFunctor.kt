package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [AccumT]; requires the underlying [monad] and the Monoid class [type]. */
interface AccumTFunctor<F, W: Monoid<*, W>> : Functor<K2<AccumT.T, F, W>> {
    /** The underlying monad. */
    val monad: Monad<F>
    /** The Monoid class of the accumulated output. */
    val type: Class<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<AccumT.T, F, W>, A>): AccumT<F, W, B> {
        val accum = AccumT.narrow(v)
        return AccumT(monad, type) { w -> monad.fmap({ (a, w1): Pair<A, W> -> Pair(f(a), w1) }, accum.run(w)) }
    }
}

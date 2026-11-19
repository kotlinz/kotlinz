package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Functor instance for [TracedT]; requires the base [Comonad] and the Monoid class of `M`. */
interface TracedTFunctor<M : Monoid<*, M>, W> : Functor<K2<TracedT.T, M, W>> {
    /** The base comonad. */
    val comonad: Comonad<W>
    /** The Monoid class of `M`. */
    val type: Class<M>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<TracedT.T, M, W>, A>): TracedT<M, W, B> {
        val tracedT = TracedT.narrow(v)
        return TracedT(comonad, type, comonad.fmap({ g: (M) -> A -> { m: M -> f(g(m)) } }, tracedT.run))
    }
}

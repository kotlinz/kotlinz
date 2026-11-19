package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [RWST]; requires the inner [monad] and the Monoid class [type] of the log. */
interface RWSTFunctor<F, R, W: Monoid<*, W>, S> : Functor<K4<RWST.T, F, R, W, S>> {
    /** The inner monad instance. */
    val monad: Monad<F>
    /** The Monoid class of the log. */
    val type: Class<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<F, R, W, S, B> {
        val rwst = RWST.narrow(v)
        return RWST(monad, type) { r, s -> monad.fmap({ (a, s2, w): Triple<A, S, W> -> Triple(f(a), s2, w) }, rwst.run(r, s)) }
    }
}

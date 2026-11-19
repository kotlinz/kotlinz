package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Applicative instance for [RWST]; effects run function first, threading state and combining logs. */
interface RWSTApplicative<F, R, W: Monoid<*, W>, S> : Applicative<K4<RWST.T, F, R, W, S>>, RWSTFunctor<F, R, W, S> {
    override fun <A> pure(v: A): RWST<F, R, W, S, A> = RWST(monad, type) { _, s -> monad.pure(Triple(v, s, Monoid.mzero(type))) }

    override fun <A, B> ap(f: K1<K4<RWST.T, F, R, W, S>, (A) -> B>, v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<F, R, W, S, B> {
        val rwstf = RWST.narrow(f)
        return RWST(monad, type) { r, s ->
            monad.bind({ (g, s2, w1): Triple<(A) -> B, S, W> ->
                monad.fmap({ (a, s3, w2): Triple<A, S, W> -> Triple(g(a), s3, Monoid.op(type, w1, w2)) }, RWST.narrow(v).run(r, s2))
            }, rwstf.run(r, s))
        }
    }
}

package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [RWST]. Stack-safety depends on the inner monad. */
interface RWSTMonad<F, R, W: Monoid<*, W>, S> : Monad<K4<RWST.T, F, R, W, S>>, RWSTApplicative<F, R, W, S>, RWSTFunctor<F, R, W, S> {
    override fun <A> join(v: K1<K4<RWST.T, F, R, W, S>, K1<K4<RWST.T, F, R, W, S>, A>>): RWST<F, R, W, S, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K4<RWST.T, F, R, W, S>, B>, v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<F, R, W, S, B> {
        val rwst = RWST.narrow(v)
        return RWST(monad, type) { r, s ->
            monad.bind({ (a, s2, w1): Triple<A, S, W> ->
                monad.fmap({ (b, s3, w2): Triple<B, S, W> -> Triple(b, s3, Monoid.op(type, w1, w2)) }, RWST.narrow(f(a)).run(r, s2))
            }, rwst.run(r, s))
        }
    }
}

package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [RWST]: lifted computations keep the state and log nothing. */
interface RWSTMonadTrans<F, R, W: Monoid<*, W>, S> : MonadTrans<F, K4<RWST.T, F, R, W, S>>, RWSTMonad<F, R, W, S> {
    override fun <A> lift(v: K1<F, A>): RWST<F, R, W, S, A> {
        return RWST(monad, type) { _, s -> monad.fmap({ a -> Triple(a, s, Monoid.mzero(type)) }, v) }
    }
}

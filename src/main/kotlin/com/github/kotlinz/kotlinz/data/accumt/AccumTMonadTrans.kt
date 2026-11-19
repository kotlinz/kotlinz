package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [AccumT]: lifted computations add no output. */
interface AccumTMonadTrans<F, W: Monoid<*, W>> : MonadTrans<F, K2<AccumT.T, F, W>>, AccumTMonad<F, W> {
    override fun <A> lift(v: K1<F, A>): AccumT<F, W, A> {
        return AccumT(monad, type) { _ -> monad.fmap({ a -> Pair(a, Monoid.mzero(type)) }, v) }
    }
}

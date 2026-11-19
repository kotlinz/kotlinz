package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [RWST]. */
interface RWSTMonadReader<F, R, W: Monoid<*, W>, S> : MonadReader<K4<RWST.T, F, R, W, S>, R>, RWSTMonad<F, R, W, S> {
    override fun ask(): RWST<F, R, W, S, R> = RWST(monad, type) { r, s -> monad.pure(Triple(r, s, Monoid.mzero(type))) }

    override fun <A> local(f: (R) -> R, v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<F, R, W, S, A> {
        val rwst = RWST.narrow(v)
        return RWST(monad, type) { r, s -> rwst.run(f(r), s) }
    }
}

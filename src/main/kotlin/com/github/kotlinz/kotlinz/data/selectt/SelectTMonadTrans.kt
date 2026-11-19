package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [SelectT]: lifted computations ignore the ranking. */
interface SelectTMonadTrans<R, F> : MonadTrans<F, K2<SelectT.T, R, F>>, SelectTMonad<R, F> {
    companion object {
        /** Creates the instance from the inner [Monad]. */
        fun <R, F> of(monad: Monad<F>): SelectTMonadTrans<R, F> {
            val base = monad
            return object : SelectTMonadTrans<R, F> { override val monad = base }
        }
    }

    override fun <A> lift(v: K1<F, A>): SelectT<R, F, A> = SelectT(monad) { v }
}

package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

/** MonadPlus instance for [LogicT]. */
interface LogicTMonadPlus<F> : MonadPlus<K1<LogicT.T, F>>, LogicTMonad<F>, LogicTAlternative<F> {
    override fun <A> mzero(): LogicT<F, A> = empty()
    override fun <A> mplus(m1: K1<K1<LogicT.T, F>, A>, m2: K1<K1<LogicT.T, F>, A>): LogicT<F, A> = orElse(m1, m2)
}

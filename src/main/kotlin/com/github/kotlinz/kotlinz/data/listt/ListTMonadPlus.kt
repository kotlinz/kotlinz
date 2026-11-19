package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

/** MonadPlus instance for [ListT]. */
interface ListTMonadPlus<F> : MonadPlus<K1<ListT.T, F>>, ListTMonad<F>, ListTAlternative<F> {
    override fun <A> mzero(): ListT<F, A> = empty()
    override fun <A> mplus(m1: K1<K1<ListT.T, F>, A>, m2: K1<K1<ListT.T, F>, A>): ListT<F, A> = orElse(m1, m2)
}

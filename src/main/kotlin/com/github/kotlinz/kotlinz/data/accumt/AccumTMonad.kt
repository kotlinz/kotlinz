package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [AccumT]. Stack-safety depends on the underlying monad. */
interface AccumTMonad<F, W: Monoid<*, W>> : Monad<K2<AccumT.T, F, W>>, AccumTApplicative<F, W>, AccumTFunctor<F, W> {
    override fun <A> join(v: K1<K2<AccumT.T, F, W>, K1<K2<AccumT.T, F, W>, A>>): AccumT<F, W, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<AccumT.T, F, W>, B>, v: K1<K2<AccumT.T, F, W>, A>): AccumT<F, W, B> {
        return chain(AccumT.narrow(v)) { a -> AccumT.narrow(f(a)) }
    }
}

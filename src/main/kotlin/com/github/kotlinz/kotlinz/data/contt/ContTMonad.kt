package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [ContT]. Not stack-safe. */
interface ContTMonad<R, F> : Monad<K2<ContT.T, R, F>>, ContTApplicative<R, F>, ContTFunctor<R, F> {
    override fun <A> join(v: K1<K2<ContT.T, R, F>, K1<K2<ContT.T, R, F>, A>>): ContT<R, F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<ContT.T, R, F>, B>, v: K1<K2<ContT.T, R, F>, A>): ContT<R, F, B> {
        val cont = ContT.narrow(v)
        return ContT(monad) { k -> cont.run { a -> ContT.narrow(f(a)).run(k) } }
    }
}

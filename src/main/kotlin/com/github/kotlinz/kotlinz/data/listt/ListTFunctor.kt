package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [ListT]; requires a `Monad<F>` for the underlying effect. */
interface ListTFunctor<F> : Functor<K1<ListT.T, F>> {
    /** The underlying monad. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<ListT.T, F>, A>): ListT<F, B> = ListT(monad) {
        monad.fmap({ step: ListTStep<F, A> -> step fmap { (a, rest) -> Pair(f(a), fmap(f, rest)) } }, ListT.narrow(v).run)
    }
}

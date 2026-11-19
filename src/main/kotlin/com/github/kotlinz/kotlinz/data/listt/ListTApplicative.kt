package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [ListT]; applies every function to every argument, running effects in order. */
interface ListTApplicative<F> : Applicative<K1<ListT.T, F>>, ListTFunctor<F> {
    override fun <A> pure(v: A): ListT<F, A> = ListT(monad) { monad.pure(Maybe.Just(Pair(v, ListT.empty(monad)))) }

    override fun <A, B> ap(f: K1<K1<ListT.T, F>, (A) -> B>, v: K1<K1<ListT.T, F>, A>): ListT<F, B> {
        return ListT.narrow(f) bind { g -> ListT.narrow(v) fmap g }
    }
}

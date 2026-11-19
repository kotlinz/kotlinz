package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [ListT]; lawful for any base monad because effects are interleaved per element. */
interface ListTMonad<F> : Monad<K1<ListT.T, F>>, ListTApplicative<F>, ListTFunctor<F> {
    override fun <A> join(v: K1<K1<ListT.T, F>, K1<K1<ListT.T, F>, A>>): ListT<F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K1<ListT.T, F>, B>, v: K1<K1<ListT.T, F>, A>): ListT<F, B> = ListT(monad) {
        monad.bind({ step: ListTStep<F, A> ->
            when (step) {
                is Maybe.Just -> (ListT.narrow(f(step.value.first)) concat bind(f, step.value.second)).run
                is Maybe.None -> monad.pure(Maybe.None())
            }
        }, ListT.narrow(v).run)
    }
}

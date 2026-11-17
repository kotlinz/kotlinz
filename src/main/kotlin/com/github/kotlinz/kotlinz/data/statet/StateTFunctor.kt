package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

interface StateTFunctor<F, S> : Functor<K2<StateT.T, F, S>> {
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, B> {
        val state = StateT.narrow(v)
        return StateT(monad) { s -> monad.fmap({ (a, s2): Pair<A, S> -> Pair(f(a), s2) }, state.run(s)) }
    }
}

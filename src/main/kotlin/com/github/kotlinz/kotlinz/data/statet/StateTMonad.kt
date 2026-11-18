package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [StateT]. Stack-safety depends on the inner monad. */
interface StateTMonad<F, S> : Monad<K2<StateT.T, F, S>>, StateTApplicative<F, S>, StateTFunctor<F, S> {
    override fun <A> join(v: K1<K2<StateT.T, F, S>, K1<K2<StateT.T, F, S>, A>>): StateT<F, S, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<StateT.T, F, S>, B>, v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, B> {
        val state = StateT.narrow(v)
        return StateT(monad) { s ->
            monad.bind({ (a, s2): Pair<A, S> -> StateT.narrow(f(a)).run(s2) }, state.run(s))
        }
    }
}

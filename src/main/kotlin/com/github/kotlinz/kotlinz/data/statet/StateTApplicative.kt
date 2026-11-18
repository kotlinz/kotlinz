package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [StateT]; effects run left to right (function first). */
interface StateTApplicative<F, S> : Applicative<K2<StateT.T, F, S>>, StateTFunctor<F, S> {
    override fun <A> pure(v: A): StateT<F, S, A> = StateT(monad) { s -> monad.pure(Pair(v, s)) }

    override fun <A, B> ap(f: K1<K2<StateT.T, F, S>, (A) -> B>, v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, B> {
        val statef = StateT.narrow(f)
        return StateT(monad) { s ->
            monad.bind({ (g, s2): Pair<(A) -> B, S> -> fmap(g, v).run(s2) }, statef.run(s))
        }
    }
}

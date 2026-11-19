package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/** MFunctor instance for [StateT], moving it from base `F` to base `G`. */
interface StateTMFunctor<F, G, S> : MFunctor<F, G, K2<StateT.T, F, S>, K2<StateT.T, G, S>> {
    /** The monad of the target base `G`. */
    val target: Monad<G>

    override fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<K2<StateT.T, F, S>, A>): StateT<G, S, A> {
        val state = StateT.narrow(v)
        return StateT(target) { s -> nt(state.run(s)) }
    }
}

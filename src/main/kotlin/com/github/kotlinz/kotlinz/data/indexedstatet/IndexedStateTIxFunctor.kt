package com.github.kotlinz.kotlinz.data.indexedstatet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.IxFunctor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** IxFunctor instance for [IndexedStateT]; requires the inner [Monad] via [monad]. */
interface IndexedStateTIxFunctor<F> : IxFunctor<K1<IndexedStateT.T, F>> {
    /** The inner monad instance. */
    val monad: Monad<F>

    override fun <I, J, A, B> imap(f: (A) -> B, v: K3<K1<IndexedStateT.T, F>, I, J, A>): IndexedStateT<F, I, J, B> {
        val state = IndexedStateT.narrow(v)
        return IndexedStateT(monad) { i -> monad.fmap({ (a, j): Pair<A, J> -> Pair(f(a), j) }, state.run(i)) }
    }
}

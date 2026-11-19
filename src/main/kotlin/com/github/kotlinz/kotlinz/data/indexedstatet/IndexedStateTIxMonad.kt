package com.github.kotlinz.kotlinz.data.indexedstatet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.IxMonad
import com.github.kotlinz.kotlinz.type.monad.Monad

/** IxMonad instance for [IndexedStateT]. Stack-safety depends on the inner monad. */
interface IndexedStateTIxMonad<F> : IxMonad<K1<IndexedStateT.T, F>>, IndexedStateTIxApplicative<F> {
    companion object {
        /** Creates the instance from the inner [Monad]. */
        fun <F> of(monad: Monad<F>): IndexedStateTIxMonad<F> {
            val base = monad
            return object : IndexedStateTIxMonad<F> { override val monad = base }
        }
    }

    override fun <I, J, K, A, B> ibind(f: (A) -> K3<K1<IndexedStateT.T, F>, J, K, B>, v: K3<K1<IndexedStateT.T, F>, I, J, A>): IndexedStateT<F, I, K, B> {
        val state = IndexedStateT.narrow(v)
        return IndexedStateT(monad) { i -> monad.bind({ (a, j): Pair<A, J> -> IndexedStateT.narrow(f(a)).run(j) }, state.run(i)) }
    }
}

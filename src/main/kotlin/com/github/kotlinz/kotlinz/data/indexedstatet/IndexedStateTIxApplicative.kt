package com.github.kotlinz.kotlinz.data.indexedstatet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.IxApplicative

/** IxApplicative instance for [IndexedStateT]; runs the function computation first. */
interface IndexedStateTIxApplicative<F> : IxApplicative<K1<IndexedStateT.T, F>>, IndexedStateTIxFunctor<F> {
    override fun <I, A> ipure(v: A): IndexedStateT<F, I, I, A> = IndexedStateT.pure(monad, v)

    override fun <I, J, K, A, B> iap(f: K3<K1<IndexedStateT.T, F>, I, J, (A) -> B>, v: K3<K1<IndexedStateT.T, F>, J, K, A>): IndexedStateT<F, I, K, B> {
        val statef = IndexedStateT.narrow(f)
        return IndexedStateT(monad) { i -> monad.bind({ (g, j): Pair<(A) -> B, J> -> imap(g, v).run(j) }, statef.run(i)) }
    }
}

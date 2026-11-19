package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [ListT]: a lifted computation becomes a one-element stream. */
interface ListTMonadTrans<F> : MonadTrans<F, K1<ListT.T, F>>, ListTMonad<F> {
    override fun <A> lift(v: K1<F, A>): ListT<F, A> = ListT(monad) {
        monad.fmap({ a: A -> Maybe.Just(Pair(a, ListT.empty<F, A>(monad))) }, v)
    }
}

package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadCont

/** MonadCont instance for [ContT]: the escape function discards its own continuation and resumes the outer one. */
interface ContTMonadCont<R, F> : MonadCont<K2<ContT.T, R, F>>, ContTMonad<R, F> {
    override fun <A, B> callCC(f: ((A) -> K1<K2<ContT.T, R, F>, B>) -> K1<K2<ContT.T, R, F>, A>): ContT<R, F, A> {
        return ContT(monad) { k -> ContT.narrow(f { a -> ContT<R, F, B>(monad) { _ -> k(a) } }).run(k) }
    }
}

package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.MonadLogic

/**
 * MonadLogic instance for [LogicT].
 *
 * [msplit] is lazy: the underlying computation only runs up to its first result when the split itself is run.
 */
interface LogicTMonadLogic<F> : MonadLogic<K1<LogicT.T, F>>, LogicTMonadPlus<F>, LogicTMonadTrans<F> {
    override fun <A> msplit(v: K1<K1<LogicT.T, F>, A>): LogicT<F, Maybe<Pair<A, K1<K1<LogicT.T, F>, A>>>> =
        deferLogic(monad) { lift(splitStep(LogicT.narrow(v))) fmap { it.fmap<Pair<A, K1<K1<LogicT.T, F>, A>>> { pair -> pair } } }
}

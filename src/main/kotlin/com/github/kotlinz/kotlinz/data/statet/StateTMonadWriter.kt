package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [StateT], lifted from a base monad that has MonadWriter. */
interface StateTMonadWriter<F, S, W> : MonadWriter<K2<StateT.T, F, S>, W>, StateTMonadTrans<F, S> {
    /** The underlying monad with output `W`. */
    override val monad: MonadWriter<F, W>

    override fun <A> writer(v: Pair<A, W>): StateT<F, S, A> = lift(monad.writer(v))

    override fun <A> listen(v: K1<K2<StateT.T, F, S>, A>): StateT<F, S, Pair<A, W>> {
        val state = StateT.narrow(v)
        return StateT(monad) { s ->
            monad.fmap({ (p, w): Pair<Pair<A, S>, W> -> Pair(Pair(p.first, w), p.second) }, monad.listen(state.run(s)))
        }
    }

    override fun <A> pass(v: K1<K2<StateT.T, F, S>, Pair<A, (W) -> W>>): StateT<F, S, A> {
        val state = StateT.narrow(v)
        return StateT(monad) { s ->
            monad.pass(monad.fmap({ (p, s2): Pair<Pair<A, (W) -> W>, S> -> Pair(Pair(p.first, s2), p.second) }, state.run(s)))
        }
    }
}

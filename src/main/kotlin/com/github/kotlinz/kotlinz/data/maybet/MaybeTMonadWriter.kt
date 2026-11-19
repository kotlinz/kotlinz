package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [MaybeT], lifted from a base monad that has MonadWriter. */
interface MaybeTMonadWriter<F, W> : MonadWriter<K1<MaybeT.T, F>, W>, MaybeTMonadTrans<F> {
    /** The underlying monad with output `W`. */
    override val monad: MonadWriter<F, W>

    override fun <A> writer(v: Pair<A, W>): MaybeT<F, A> = lift(monad.writer(v))

    override fun <A> listen(v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, Pair<A, W>> {
        val run = monad.fmap({ (m, w): Pair<Maybe<A>, W> -> m fmap { a -> Pair(a, w) } }, monad.listen(MaybeT.narrow(v).run))
        return MaybeT(monad, run)
    }

    override fun <A> pass(v: K1<K1<MaybeT.T, F>, Pair<A, (W) -> W>>): MaybeT<F, A> {
        return MaybeT(monad, monad.pass(monad.fmap({ m: Maybe<Pair<A, (W) -> W>> -> passing(m) }, MaybeT.narrow(v).run)))
    }

    private fun <A> passing(m: Maybe<Pair<A, (W) -> W>>): Pair<Maybe<A>, (W) -> W> = when (m) {
        is Maybe.Just -> Pair(Maybe.Just(m.value.first), m.value.second)
        is Maybe.None -> Pair(Maybe.None(), { w -> w })
    }
}

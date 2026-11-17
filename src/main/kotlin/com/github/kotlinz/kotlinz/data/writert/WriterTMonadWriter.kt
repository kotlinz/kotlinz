package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

interface WriterTMonadWriter<F, W: Monoid<*, W>> : MonadWriter<K2<WriterT.T, F, W>, W>, WriterTMonad<F, W> {
    override fun <A> writer(v: Pair<A, W>): WriterT<F, W, A> = WriterT(monad, type, monad.pure(v))

    override fun <A> listen(v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, Pair<A, W>> {
        val run = monad.fmap({ (a, w): Pair<A, W> -> Pair(Pair(a, w), w) }, WriterT.narrow(v).run)
        return WriterT(monad, type, run)
    }

    override fun <A> pass(v: K1<K2<WriterT.T, F, W>, Pair<A, (W) -> W>>): WriterT<F, W, A> {
        val run = monad.fmap({ (pair, w): Pair<Pair<A, (W) -> W>, W> -> Pair(pair.first, pair.second(w)) }, WriterT.narrow(v).run)
        return WriterT(monad, type, run)
    }

    override fun tell(w: W): WriterT<F, W, Unit> = writer(Pair(Unit, w))
}

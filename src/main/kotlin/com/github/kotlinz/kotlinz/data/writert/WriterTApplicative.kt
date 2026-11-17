package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

interface WriterTApplicative<F, W: Monoid<*, W>> : Applicative<K2<WriterT.T, F, W>>, WriterTFunctor<F, W> {
    override fun <A> pure(v: A): WriterT<F, W, A> = WriterT(monad, type, monad.pure(Pair(v, Monoid.mzero(type))))

    override fun <A, B> ap(f: K1<K2<WriterT.T, F, W>, (A) -> B>, v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, B> {
        val run = monad.bind({ (g, w1): Pair<(A) -> B, W> ->
            monad.fmap({ (a, w2): Pair<A, W> -> Pair(g(a), Monoid.op(type, w1, w2)) }, WriterT.narrow(v).run)
        }, WriterT.narrow(f).run)
        return WriterT(monad, type, run)
    }
}

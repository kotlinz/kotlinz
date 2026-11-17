package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

interface WriterTMonad<F, W: Monoid<*, W>> : Monad<K2<WriterT.T, F, W>>, WriterTApplicative<F, W>, WriterTFunctor<F, W> {
    override fun <A> join(v: K1<K2<WriterT.T, F, W>, K1<K2<WriterT.T, F, W>, A>>): WriterT<F, W, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<WriterT.T, F, W>, B>, v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, B> {
        val run = monad.bind({ (a, w1): Pair<A, W> ->
            monad.fmap({ (b, w2): Pair<B, W> -> Pair(b, Monoid.op(type, w1, w2)) }, WriterT.narrow(f(a)).run)
        }, WriterT.narrow(v).run)
        return WriterT(monad, type, run)
    }
}

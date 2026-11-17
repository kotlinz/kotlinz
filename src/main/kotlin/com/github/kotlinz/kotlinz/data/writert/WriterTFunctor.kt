package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

interface WriterTFunctor<F, W: Monoid<*, W>> : Functor<K2<WriterT.T, F, W>> {
    val monad: Monad<F>
    val type: Class<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<WriterT.T, F, W>, A>): WriterT<F, W, B> {
        val run = monad.fmap({ (a, w): Pair<A, W> -> Pair(f(a), w) }, WriterT.narrow(v).run)
        return WriterT(monad, type, run)
    }
}

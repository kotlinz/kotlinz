package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [TracedT]; extends the base comonad and shifts each inner offset by the outer one. */
interface TracedTComonad<M : Monoid<*, M>, W> : Comonad<K2<TracedT.T, M, W>>, TracedTFunctor<M, W> {
    override fun <A> extract(v: K1<K2<TracedT.T, M, W>, A>): A = TracedT.narrow(v).trace(Monoid.mzero(type))

    override fun <A> duplicate(v: K1<K2<TracedT.T, M, W>, A>): TracedT<M, W, K1<K2<TracedT.T, M, W>, A>> = extend({ it }, v)

    override fun <A, B> extend(f: (K1<K2<TracedT.T, M, W>, A>) -> B, v: K1<K2<TracedT.T, M, W>, A>): TracedT<M, W, B> {
        val shift = { w: K1<W, (M) -> A>, m: M -> TracedT(comonad, type, comonad.fmap({ g: (M) -> A -> { m2: M -> g(Monoid.op(type, m, m2)) } }, w)) }
        return TracedT(comonad, type, comonad.extend({ w: K1<W, (M) -> A> -> { m: M -> f(shift(w, m)) } }, TracedT.narrow(v).run))
    }
}

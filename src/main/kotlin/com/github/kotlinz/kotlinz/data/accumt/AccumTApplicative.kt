package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Applicative instance for [AccumT]; the function runs first and its output is visible to the argument. */
interface AccumTApplicative<F, W: Monoid<*, W>> : Applicative<K2<AccumT.T, F, W>>, AccumTFunctor<F, W> {
    override fun <A> pure(v: A): AccumT<F, W, A> = AccumT(monad, type) { _ -> monad.pure(Pair(v, Monoid.mzero(type))) }

    override fun <A, B> ap(f: K1<K2<AccumT.T, F, W>, (A) -> B>, v: K1<K2<AccumT.T, F, W>, A>): AccumT<F, W, B> {
        return chain(AccumT.narrow(f)) { g -> fmap(g, v) }
    }

    /** Runs [v], then [f] with the output of [v] appended to what it sees, combining the new outputs. */
    fun <A, B> chain(v: AccumT<F, W, A>, f: (A) -> AccumT<F, W, B>): AccumT<F, W, B> = AccumT(monad, type) { w ->
        monad.bind({ (a, w1): Pair<A, W> ->
            monad.fmap({ (b, w2): Pair<B, W> -> Pair(b, Monoid.op(type, w1, w2)) }, f(a).run(Monoid.op(type, w, w1)))
        }, v.run(w))
    }
}

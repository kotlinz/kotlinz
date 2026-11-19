package com.github.kotlinz.kotlinz.data.rwst

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [RWST]. */
interface RWSTMonadWriter<F, R, W: Monoid<*, W>, S> : MonadWriter<K4<RWST.T, F, R, W, S>, W>, RWSTMonad<F, R, W, S> {
    override fun <A> writer(v: Pair<A, W>): RWST<F, R, W, S, A> = RWST(monad, type) { _, s -> monad.pure(Triple(v.first, s, v.second)) }

    override fun <A> listen(v: K1<K4<RWST.T, F, R, W, S>, A>): RWST<F, R, W, S, Pair<A, W>> {
        val rwst = RWST.narrow(v)
        return RWST(monad, type) { r, s -> monad.fmap({ (a, s2, w): Triple<A, S, W> -> Triple(Pair(a, w), s2, w) }, rwst.run(r, s)) }
    }

    override fun <A> pass(v: K1<K4<RWST.T, F, R, W, S>, Pair<A, (W) -> W>>): RWST<F, R, W, S, A> {
        val rwst = RWST.narrow(v)
        return RWST(monad, type) { r, s ->
            monad.fmap({ (p, s2, w): Triple<Pair<A, (W) -> W>, S, W> -> Triple(p.first, s2, p.second(w)) }, rwst.run(r, s))
        }
    }

    override fun tell(w: W): RWST<F, R, W, S, Unit> = writer(Pair(Unit, w))
}

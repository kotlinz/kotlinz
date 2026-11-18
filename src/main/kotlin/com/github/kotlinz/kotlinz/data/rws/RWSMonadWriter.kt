package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

/** MonadWriter instance for [RWS]. */
interface RWSMonadWriter<R, W: Monoid<*, W>, S>: MonadWriter<K3<RWS.T, R, W, S>, W>, RWSMonad<R, W, S> {
  override fun <A> writer(v: Pair<A, W>): RWS<R, W, S, A> = RWS(type) { _, s -> Triple(v.first, s, v.second) }

  override fun <A> listen(v: K1<K3<RWS.T, R, W, S>, A>): RWS<R, W, S, Pair<A, W>> {
    val rws = RWS.narrow(v)
    return RWS(type) { r, s -> rws.run(r, s).let { (a, s2, w) -> Triple(Pair(a, w), s2, w) } }
  }

  override fun <A> pass(v: K1<K3<RWS.T, R, W, S>, Pair<A, (W) -> W>>): RWS<R, W, S, A> {
    val rws = RWS.narrow(v)
    return RWS(type) { r, s -> rws.run(r, s).let { (pair, s2, w) -> Triple(pair.first, s2, pair.second(w)) } }
  }

  override fun tell(w: W): RWS<R, W, S, Unit> = writer(Pair(Unit, w))
}

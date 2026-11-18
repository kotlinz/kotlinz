package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadReader

/** MonadReader instance for [RWS]. */
interface RWSMonadReader<R, W: Monoid<*, W>, S>: MonadReader<K3<RWS.T, R, W, S>, R>, RWSMonad<R, W, S> {
  override fun ask(): RWS<R, W, S, R> = RWS(type) { r, s -> Triple(r, s, Monoid.mzero(type)) }

  override fun <A> local(f: (R) -> R, v: K1<K3<RWS.T, R, W, S>, A>): RWS<R, W, S, A> {
    val rws = RWS.narrow(v)
    return RWS(type) { r, s -> rws.run(f(r), s) }
  }
}

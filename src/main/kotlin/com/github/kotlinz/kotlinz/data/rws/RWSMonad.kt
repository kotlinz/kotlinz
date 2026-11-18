package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [RWS]; logs are combined with the Monoid of [type]. */
interface RWSMonad<R, W: Monoid<*, W>, S>: Monad<K3<RWS.T, R, W, S>>, RWSApplicative<R, W, S>, RWSFunctor<R, W, S> {
  override val type: Class<W>

  override fun <A> join(v: K1<K3<RWS.T, R, W, S>, K1<K3<RWS.T, R, W, S>, A>>): RWS<R, W, S, A> {
    val rws = RWS.narrow(v)
    return RWS(type) { r, s ->
      val (value, s2, w) = rws.run(r, s)
      val (value2, s3, w2) = RWS.narrow(value).run(r, s2)
      Triple(value2, s3, Monoid.op(type, w, w2))
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K3<RWS.T, R, W, S>, B>, v: K1<K3<RWS.T, R, W, S>, A>): RWS<R, W, S, B> {
    return RWS.narrow(super.bind(f, v))
  }
}

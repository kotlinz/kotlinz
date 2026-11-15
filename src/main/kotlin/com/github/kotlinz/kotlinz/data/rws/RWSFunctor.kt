package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid

interface RWSFunctor<R, W: Monoid<*, W>, S>: Functor<K3<RWS.T, R, W, S>> {
  val type: Class<W>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K3<RWS.T, R, W, S>, A>): RWS<R, W, S, B> {
    return RWS(type) { r, s ->
      val (value, s2, w) = RWS.narrow(v).run(r, s)
      Triple(f(value), s2, w)
    }
  }
}

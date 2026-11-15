package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

interface RWSApplicative<R, W: Monoid<*, W>, S>: Applicative<K3<RWS.T, R, W, S>> {
  val type: Class<W>

  override fun <A> pure(v: A): RWS<R, W, S, A> = RWS(type) { r, s -> Triple(v, s, Monoid.mzero(type)) }

  override fun <A, B> ap(f: K1<K3<RWS.T, R, W, S>, (A) -> B>, v: K1<K3<RWS.T, R, W, S>, A>): RWS<R, W, S, B> {
    val rws = RWS.narrow(v)
    val rwsf = RWS.narrow(f)

    return RWS(type) { r, s ->
      val (value, s2, w) = rwsf.run(r, s)
      val (value2, s3, w2) = RWS.narrow(fmap(value, rws)).run(r, s2)
      Triple(value2, s3, Monoid.op(type, w, w2))
    }
  }
}

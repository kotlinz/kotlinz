package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface TracedComonad<M: Monoid<*, M>>: Comonad<K1<Traced.T, M>>, TracedCopointed<M>, TracedFunctor<M> {
  override val type: Class<M>

  override fun <A> duplicate(v: K1<K1<Traced.T, M>, A>): Traced<M, K1<K1<Traced.T, M>, A>> {
    val traced = Traced.narrow(v)
    return Traced(type) { m -> Traced(type) { m2 -> traced.run(Monoid.op(type, m, m2)) } }
  }

  override fun <A, B> extend(f: (K1<K1<Traced.T, M>, A>) -> B, v: K1<K1<Traced.T, M>, A>): Traced<M, B> {
    return Traced.narrow(fmap(f, duplicate(v)))
  }
}

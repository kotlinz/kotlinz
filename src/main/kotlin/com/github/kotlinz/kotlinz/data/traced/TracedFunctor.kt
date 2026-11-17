package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid

interface TracedFunctor<M: Monoid<*, M>>: Functor<K1<Traced.T, M>> {
  val type: Class<M>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Traced.T, M>, A>): Traced<M, B> {
    val traced = Traced.narrow(v)
    return Traced(type) { m -> f(traced.run(m)) }
  }
}

package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Star]: the untouched component is restored after the effect. */
interface StarStrong<F>: Strong<Star.T, F>, StarProfunctor<F> {
  override fun <A, B, C> first(v: K3<Star.T, F, A, B>): Star<F, Pair<A, C>, Pair<B, C>> {
    val star = Star.narrow(v)
    return Star { (a, c) -> functor.fmap({ b: B -> Pair(b, c) }, star.run(a)) }
  }

  override fun <A, B, C> second(v: K3<Star.T, F, A, B>): Star<F, Pair<C, A>, Pair<C, B>> = Star.narrow(super.second(v))
}

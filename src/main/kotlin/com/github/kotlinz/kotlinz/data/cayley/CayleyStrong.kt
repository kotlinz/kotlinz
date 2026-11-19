package com.github.kotlinz.kotlinz.data.cayley

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Cayley]: lifts `first` through the functor. */
interface CayleyStrong<F, PT, PF>: Strong<Cayley.T, Cayley.Of<F, PT, PF>>, CayleyProfunctor<F, PT, PF> {
  override val profunctor: Strong<PT, PF>

  override fun <A, B, C> first(v: K3<Cayley.T, Cayley.Of<F, PT, PF>, A, B>): Cayley<F, PT, PF, Pair<A, C>, Pair<B, C>> =
    Cayley(functor.fmap({ pab: K3<PT, PF, A, B> -> profunctor.first<A, B, C>(pab) }, Cayley.narrow(v).value))

  override fun <A, B, C> second(v: K3<Cayley.T, Cayley.Of<F, PT, PF>, A, B>): Cayley<F, PT, PF, Pair<C, A>, Pair<C, B>> =
    Cayley.narrow(super.second(v))
}

package com.github.kotlinz.kotlinz.data.procompose

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Procompose]: both steps thread the extra component. */
interface ProcomposeStrong<PT, PF, QT, QF>: Strong<Procompose.T, Procompose.Of<PT, PF, QT, QF>>, ProcomposeProfunctor<PT, PF, QT, QF> {
  override val p: Strong<PT, PF>
  override val q: Strong<QT, QF>

  override fun <A, B, C> first(v: K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, A, B>): Procompose<PT, PF, QT, QF, Pair<A, C>, Pair<B, C>> {
    val composed = Procompose.narrow(v)
    return Procompose.of(p.first<Any?, B, C>(composed.outer), q.first<A, Any?, C>(composed.inner))
  }

  override fun <A, B, C> second(v: K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, A, B>): Procompose<PT, PF, QT, QF, Pair<C, A>, Pair<C, B>> =
    Procompose.narrow(super.second(v))
}

package com.github.kotlinz.kotlinz.data.procompose

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Procompose]: maps the input through [q] and the output through [p]. */
interface ProcomposeProfunctor<PT, PF, QT, QF>: Profunctor<Procompose.T, Procompose.Of<PT, PF, QT, QF>> {
  /** The profunctor of the outer step. */
  val p: Profunctor<PT, PF>
  /** The profunctor of the inner step. */
  val q: Profunctor<QT, QF>

  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, B, C>): Procompose<PT, PF, QT, QF, A, D> {
    val composed = Procompose.narrow(v)
    return Procompose.of(p.rmap(g, composed.outer), q.lmap(f, composed.inner))
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, B, C>): Procompose<PT, PF, QT, QF, A, C> =
    Procompose.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, A, B>): Procompose<PT, PF, QT, QF, A, C> =
    Procompose.narrow(super.rmap(g, v))
}

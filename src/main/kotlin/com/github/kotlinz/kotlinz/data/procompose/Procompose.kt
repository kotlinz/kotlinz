package com.github.kotlinz.kotlinz.data.procompose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose

/**
 * The composition of two profunctors: an [inner] `Q<A, X>` followed by an [outer] `P<X, C>` for some
 * hidden middle type `X`.
 *
 * `P` is identified by the tags `PT, PF` (values `K3<PT, PF, _, _>`) and `Q` by `QT, QF`; the pair of
 * tags is bundled into the phantom [Of] so that `Procompose` fits `Profunctor<T, F>`.
 *
 * Instances: [ProcomposeProfunctor], [ProcomposeStrong].
 *
 * ```kotlin
 * val composed = Procompose.of(Function1 { n: Int -> n * 2 }, Function1 { s: String -> s.length })
 * Function1.narrow(composed.collapse(object : Function1Compose {}))("abc") // 6
 * ```
 *
 * @property outer the second step, from the hidden middle type to `C`.
 * @property inner the first step, from `A` to the hidden middle type.
 */
class Procompose<PT, PF, QT, QF, A, C> private constructor(
  val outer: K3<PT, PF, Any?, C>,
  val inner: K3<QT, QF, A, Any?>
): K3<Procompose.T, Procompose.Of<PT, PF, QT, QF>, A, C> {
  class T
  /** Phantom bundling the tags of both profunctors. */
  class Of<PT, PF, QT, QF>

  companion object {
    /** Downcasts a higher-kinded value back to [Procompose]. */
    fun <PT, PF, QT, QF, A, C> narrow(v: K1<K2<T, Of<PT, PF, QT, QF>, A>, C>): Procompose<PT, PF, QT, QF, A, C> =
      v as Procompose<PT, PF, QT, QF, A, C>

    /** Composes [outer] after [inner], hiding the middle type `B`. */
    @Suppress("UNCHECKED_CAST")
    fun <PT, PF, QT, QF, A, B, C> of(outer: K3<PT, PF, B, C>, inner: K3<QT, QF, A, B>): Procompose<PT, PF, QT, QF, A, C> =
      Procompose(outer as K3<PT, PF, Any?, C>, inner as K3<QT, QF, A, Any?>)
  }
}

/** Collapses a composition of the same profunctor back into a single value using [compose]. */
fun <T, F, A, C> Procompose<T, F, T, F, A, C>.collapse(compose: Compose<T, F>): K3<T, F, A, C> = compose.compose(outer, inner)

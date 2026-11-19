package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander

/** Traversing instance for [Star]: runs the traversal in the star's applicative. */
interface StarTraversing<F>: Traversing<Star.T, F>, StarStrong<F>, StarChoice<F> {
  override fun <S, R, A, B> wander(traversal: Wander<S, R, A, B>, v: K3<Star.T, F, A, B>): Star<F, S, R> {
    val star = Star.narrow(v)
    return Star { s -> traversal.run(applicative, star.run, s) }
  }
}

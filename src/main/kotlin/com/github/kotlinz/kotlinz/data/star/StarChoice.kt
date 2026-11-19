package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** Choice instance for [Star]: the skipped branch is lifted with `pure`. */
interface StarChoice<F>: Choice<Star.T, F>, StarProfunctor<F> {
  val applicative: Applicative<F>
  override val functor: Functor<F> get() = applicative

  override fun <A, B, C> left(v: K3<Star.T, F, A, B>): Star<F, Either<A, C>, Either<B, C>> {
    val star = Star.narrow(v)
    return Star { e ->
      when (e) {
        is Either.Left -> applicative.fmap({ b: B -> Either.Left<B, C>(b) }, star.run(e.value))
        is Either.Right -> applicative.pure(Either.Right(e.value))
      }
    }
  }

  override fun <A, B, C> right(v: K3<Star.T, F, A, B>): Star<F, Either<C, A>, Either<C, B>> = Star.narrow(super.right(v))
}

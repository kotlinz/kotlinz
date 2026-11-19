package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.type.show.Show

/** [Show] instance for [Either], rendered as `Left(l)` or `Right(r)`. */
interface EitherShow<L, R>: Show<Either<L, R>> {
  val showL: Show<L>
  val showR: Show<R>

  override fun show(a: Either<L, R>): String = when (a) {
    is Either.Left -> "Left(${showL.show(a.value)})"
    is Either.Right -> "Right(${showR.show(a.value)})"
  }
}

/** Returns the [Show] instance for [Either] derived from the given element instances. */
fun <L, R> Either.Companion.show(showL: Show<L>, showR: Show<R>): Show<Either<L, R>> = object: EitherShow<L, R> { override val showL = showL; override val showR = showR }

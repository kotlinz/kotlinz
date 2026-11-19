package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Either]: same side and equivalent values under [eqL] / [eqR]. */
interface EitherEq<L, R>: Eq<Either<L, R>> {
  val eqL: Eq<L>
  val eqR: Eq<R>

  override fun eqv(a: Either<L, R>, b: Either<L, R>): Boolean = when {
    a is Either.Left && b is Either.Left -> eqL.eqv(a.value, b.value)
    a is Either.Right && b is Either.Right -> eqR.eqv(a.value, b.value)
    else -> false
  }
}

/** Returns the [Eq] instance for [Either] derived from the given element instances. */
fun <L, R> Either.Companion.eq(eqL: Eq<L>, eqR: Eq<R>): Eq<Either<L, R>> = object: EitherEq<L, R> { override val eqL = eqL; override val eqR = eqR }

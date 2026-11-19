package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * Selective instance for [Validated]. An [Validated.Invalid] first value stops without inspecting
 * the function, `Valid(Right)` skips it, and only `Valid(Left)` runs it. Requires the Semigroup
 * class via [type], inherited from [ValidatedApplicative].
 */
interface ValidatedSelective<E: Semigroup<*, E>>: Selective<K1<Validated.T, E>>, ValidatedApplicative<E> {
  override fun <A, B> select(fab: K1<K1<Validated.T, E>, Either<A, B>>, fn: K1<K1<Validated.T, E>, (A) -> B>): Validated<E, B> =
    when (val v = Validated.narrow(fab)) {
      is Validated.Invalid -> Validated.Invalid(v.error)
      is Validated.Valid -> selectValid(v.value, fn)
    }

  private fun <A, B> selectValid(e: Either<A, B>, fn: K1<K1<Validated.T, E>, (A) -> B>): Validated<E, B> = when (e) {
    is Either.Left -> fmap({ f -> f(e.value) }, fn)
    is Either.Right -> Validated.Valid(e.value)
  }
}

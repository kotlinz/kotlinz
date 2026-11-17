package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.group.Semigroup

sealed class Validated<E, A>: K2<Validated.T, E, A> {
  class T

  data class Valid<E, A>(val value: A): Validated<E, A>()
  data class Invalid<E, A>(val error: E): Validated<E, A>()

  companion object {
    fun <E, A> narrow(v: K1<K1<T, E>, A>) = v as Validated<E, A>

    fun <E, A> valid(v: A): Validated<E, A> = Valid(v)
    fun <E, A> invalid(e: E): Validated<E, A> = Invalid(e)
    fun <E, A> fromEither(v: Either<E, A>): Validated<E, A> = when (v) {
      is Either.Left -> Invalid(v.value)
      is Either.Right -> Valid(v.value)
    }

    // Applicative
    fun <E: Semigroup<*, E>, A> pure(type: Class<E>, v: A): Validated<E, A> = applicative(type).pure(v)
    private fun <E: Semigroup<*, E>> applicative(type: Class<E>) = object: ValidatedApplicative<E> { override val type = type }

    // ApplicativeOps
    fun <E: Semigroup<*, E>, A, B> liftA(type: Class<E>, f: (A) -> B) = applicativeOps(type).liftA(f)
    fun <E: Semigroup<*, E>, A, B, C> liftA2(type: Class<E>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    fun <E: Semigroup<*, E>, A, B, C, D> liftA3(type: Class<E>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <E: Semigroup<*, E>> applicativeOps(type: Class<E>) = object: ValidatedApplicativeOps<E> { override val type = type }
  }

  // Functor
  infix fun <B> fmap(f: (A) -> B): Validated<E, B> = functor.fmap(f, this)
  private val functor = object: ValidatedFunctor<E> {}

  fun toEither(): Either<E, A> = when (this) {
    is Valid -> Either.Right(value)
    is Invalid -> Either.Left(error)
  }
}

fun <E: Semigroup<*, E>, A, B> Validated<E, A>.ap(type: Class<E>, f: Validated<E, (A) -> B>): Validated<E, B> =
  object: ValidatedApplicative<E> { override val type = type }.ap(f, this)

fun <E, A> Either<E, A>.toValidated(): Validated<E, A> = Validated.fromEither(this)

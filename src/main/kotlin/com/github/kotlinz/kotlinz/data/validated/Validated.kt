package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * Either a [Valid] value or an [Invalid] error, with error-accumulating applicative composition.
 *
 * Unlike [Either], combining several `Validated` values keeps going after the first failure and
 * merges all errors with the [com.github.kotlinz.kotlinz.type.group.Semigroup] `E`. Use it for
 * form or config validation where every problem should be reported. It intentionally has no Monad
 * instance, because `bind` cannot accumulate errors.
 *
 * Instances: [ValidatedFunctor], [ValidatedApplicative], [ValidatedApplicativeOps] (the latter two
 * require a Semigroup error type).
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val add = Validated.liftA2<StringMonoid, Int, Int, Int>(type) { a, b -> a + b }
 * add(Validated.valid(1), Validated.valid(2))                 // Valid(3)
 * add(Validated.invalid(StringMonoid("e1")), Validated.invalid(StringMonoid("e2")))
 *                                                             // Invalid(StringMonoid("e1e2"))
 * ```
 */
sealed class Validated<E, A>: K2<Validated.T, E, A> {
  /** Higher-kinded witness for [Validated]. */
  class T

  /** A successful value. */
  data class Valid<E, A>(val value: A): Validated<E, A>()
  /** A failure carrying an error. */
  data class Invalid<E, A>(val error: E): Validated<E, A>()

  companion object {
    /** Downcasts a higher-kinded value to [Validated]. */
    fun <E, A> narrow(v: K1<K1<T, E>, A>) = v as Validated<E, A>

    /** Creates a [Valid]. */
    fun <E, A> valid(v: A): Validated<E, A> = Valid(v)
    /** Creates an [Invalid]. */
    fun <E, A> invalid(e: E): Validated<E, A> = Invalid(e)
    /** Converts `Left` to [Invalid] and `Right` to [Valid]. */
    fun <E, A> fromEither(v: Either<E, A>): Validated<E, A> = when (v) {
      is Either.Left -> Invalid(v.value)
      is Either.Right -> Valid(v.value)
    }

    // Applicative
    /** Wraps [v] as [Valid]. */
    fun <E: Semigroup<*, E>, A> pure(type: Class<E>, v: A): Validated<E, A> = applicative(type).pure(v)
    private fun <E: Semigroup<*, E>> applicative(type: Class<E>) = object: ValidatedApplicative<E> { override val type = type }

    // ApplicativeOps
    /** Lifts a unary function into [Validated]. */
    fun <E: Semigroup<*, E>, A, B> liftA(type: Class<E>, f: (A) -> B) = applicativeOps(type).liftA(f)
    /** Lifts a binary function into [Validated], accumulating errors. */
    fun <E: Semigroup<*, E>, A, B, C> liftA2(type: Class<E>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    /** Lifts a ternary function into [Validated], accumulating errors. */
    fun <E: Semigroup<*, E>, A, B, C, D> liftA3(type: Class<E>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <E: Semigroup<*, E>> applicativeOps(type: Class<E>) = object: ValidatedApplicativeOps<E> { override val type = type }
  }

  // Functor
  /** Maps the valid value. */
  infix fun <B> fmap(f: (A) -> B): Validated<E, B> = functor.fmap(f, this)
  private val functor = object: ValidatedFunctor<E> {}

  /** Converts [Valid] to `Right` and [Invalid] to `Left`. */
  fun toEither(): Either<E, A> = when (this) {
    is Valid -> Either.Right(value)
    is Invalid -> Either.Left(error)
  }
}

/** Applies the function in [f], accumulating errors from both sides. */
fun <E: Semigroup<*, E>, A, B> Validated<E, A>.ap(type: Class<E>, f: Validated<E, (A) -> B>): Validated<E, B> =
  object: ValidatedApplicative<E> { override val type = type }.ap(f, this)

/** Converts an [Either] to [Validated]. */
fun <E, A> Either<E, A>.toValidated(): Validated<E, A> = Validated.fromEither(this)

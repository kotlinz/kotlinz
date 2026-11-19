package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Choice
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.tagged.Tagged
import com.github.kotlinz.kotlinz.data.tagged.TaggedChoice
import com.github.kotlinz.kotlinz.type.profunctor.Choice
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/**
 * A focus on one case of a sum type: an `S` may contain an `A`, and a `B` always builds a `T`.
 *
 * Profunctor encoding: a `Prism` lifts any [Choice] profunctor value `P<A, B>` to `P<S, T>` via
 * the generic method [runChoice]. Use the [Prism] factories and the [getMaybe] / [reverseGet] /
 * [modify] helpers; ready-made prisms for `Maybe` and `Either` live on the companion.
 *
 * Laws:
 * - `getMaybe(reverseGet(a)) == Just(a)`
 * - If `getMaybe(s) == Just(a)` then `reverseGet(a) == s`
 *
 * ```kotlin
 * val int = Prism({ s: String -> s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None() }, { n: Int -> n.toString() })
 * int.getMaybe("42")             // Just(42)
 * int.getMaybe("x")              // None
 * int.reverseGet(7)              // "7"
 * int.modify { it + 1 }("41")    // "42"
 * ```
 */
interface Prism<S, T, A, B> : Optional<S, T, A, B> {
  /** Lifts [pab] through this prism using [choice]. */
  fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>

  override fun <P, F, Q> runOptional(profunctor: Q, pab: K3<P, F, A, B>): K3<P, F, S, T> where Q : Strong<P, F>, Q : Choice<P, F> =
    runChoice(profunctor, pab)

  /** Hosts ready-made prisms such as [just], [left] and [right]. */
  companion object
}

/** Builds a simple [Prism] from a partial getter and a constructor. */
fun <S, A> Prism(getMaybe: (S) -> Maybe<A>, reverseGet: (A) -> S): Prism<S, S, A, A> =
  Prism.matching({ s: S -> getMaybe(s).toEither(s) }, reverseGet)

/** Builds a polymorphic [Prism]: [match] returns the focus or the already rebuilt `T`. */
fun <S, T, A, B> Prism.Companion.matching(match: (S) -> Either<T, A>, reverseGet: (B) -> T): Prism<S, T, A, B> =
  object : Prism<S, T, A, B> {
    override fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> =
      choice.dimap(match, { e: Either<T, B> -> e.fold({ it }, reverseGet) }, choice.right<A, B, T>(pab))
  }

/** Returns the focus if this case matches. */
fun <S, T, A, B> Prism<S, T, A, B>.getMaybe(s: S): Maybe<A> =
  Forget.narrow(runChoice(firstForget<A>(),Forget<Maybe<A>, A, B> { Maybe.Just(it) })).run(s)

/** Builds a `T` from a `B`. */
fun <S, T, A, B> Prism<S, T, A, B>.reverseGet(b: B): T =
  Tagged.narrow(runChoice(object : TaggedChoice {}, Tagged<A, B>(b))).value

/** Modifies the focus with [f] if this case matches, otherwise leaves the value unchanged. */
fun <S, T, A, B> Prism<S, T, A, B>.modify(f: (A) -> B): (S) -> T =
  Function1.narrow(runChoice(object : Function1Choice {}, Function1(f))).run

/** Composes with [other], focusing first through this prism and then through [other]. */
infix fun <S, T, A, B, C, D> Prism<S, T, A, B>.andThen(other: Prism<A, B, C, D>): Prism<S, T, C, D> = object : Prism<S, T, C, D> {
  override fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, C, D>): K3<P, F, S, T> =
    this@andThen.runChoice(choice, other.runChoice(choice, pab))
}

private fun <A, S> Maybe<A>.toEither(s: S): Either<S, A> = when (this) {
  is Maybe.Just -> Either.Right(value)
  is Maybe.None -> Either.Left(s)
}

internal fun <L, R, X> Either<L, R>.fold(onLeft: (L) -> X, onRight: (R) -> X): X = when (this) {
  is Either.Left -> onLeft(value)
  is Either.Right -> onRight(value)
}

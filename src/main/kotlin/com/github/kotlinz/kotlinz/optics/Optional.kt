package com.github.kotlinz.kotlinz.optics

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Traversing
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.profunctor.Choice
import com.github.kotlinz.kotlinz.type.profunctor.Strong
import com.github.kotlinz.kotlinz.type.profunctor.Traversing

/**
 * A focus on at most one `A` inside an `S` (an affine traversal), e.g. a lens followed by a prism.
 *
 * Profunctor encoding: an `Optional` lifts `P<A, B>` to `P<S, T>` for any profunctor that is both
 * [Strong] and [Choice], via the generic method [runOptional]. Every [Lens] and [Prism] is an
 * `Optional`, so `lens andThen prism` yields one; every `Optional` is a [Traversal].
 *
 * Laws:
 * - Get-set: `getMaybe(set(a)(s)) == getMaybe(s).fmap { a }`
 * - Set-get: if `getMaybe(s) == Just(a)` then `set(a)(s) == s`
 * - Set-set: `set(a2)(set(a1)(s)) == set(a2)(s)`
 *
 * ```kotlin
 * val head = Optional({ xs: List<Int> -> xs.firstOrNull()?.let { Maybe.Just(it) } ?: Maybe.None() },
 *   { xs: List<Int>, x: Int -> if (xs.isEmpty()) xs else listOf(x) + xs.drop(1) })
 * head.getMaybe(listOf(1, 2))        // Just(1)
 * head.set(9)(listOf(1, 2))          // [9, 2]
 * head.modify { it + 1 }(listOf())   // []
 * ```
 */
interface Optional<S, T, A, B> : Traversal<S, T, A, B> {
  /** Lifts [pab] through this optional using a strong and choice [profunctor]. */
  fun <P, F, Q> runOptional(profunctor: Q, pab: K3<P, F, A, B>): K3<P, F, S, T> where Q : Strong<P, F>, Q : Choice<P, F>

  override fun <P, F> runTraversing(traversing: Traversing<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T> =
    runOptional(traversing, pab)

  /** Hosts optional-related constructors such as [matching]. */
  companion object
}

/** Builds a simple [Optional] from a partial getter and a setter (applied only when the focus exists). */
fun <S, A> Optional(getMaybe: (S) -> Maybe<A>, set: (S, A) -> S): Optional<S, S, A, A> =
  Optional.matching({ s: S -> getMaybe(s).let { if (it is Maybe.Just) Either.Right(it.value) else Either.Left(s) } }, set)

/** Builds a polymorphic [Optional]: [match] returns the focus or the already rebuilt `T`. */
fun <S, T, A, B> Optional.Companion.matching(match: (S) -> Either<T, A>, set: (S, B) -> T): Optional<S, T, A, B> =
  object : Optional<S, T, A, B> {
    override fun <P, F, Q> runOptional(profunctor: Q, pab: K3<P, F, A, B>): K3<P, F, S, T> where Q : Strong<P, F>, Q : Choice<P, F> =
      profunctor.dimap(
        { s: S -> Pair(s, match(s)) },
        { (s, e): Pair<S, Either<T, B>> -> e.fold({ it }, { b -> set(s, b) }) },
        profunctor.second<Either<T, A>, Either<T, B>, S>(profunctor.right<A, B, T>(pab))
      )
  }

/** Returns the focus if present. */
fun <S, T, A, B> Optional<S, T, A, B>.getMaybe(s: S): Maybe<A> =
  Forget.narrow(runOptional(firstForget<A>(), Forget<Maybe<A>, A, B> { Maybe.Just(it) })).run(s)

/** Replaces the focus with [b] if present. */
fun <S, T, A, B> Optional<S, T, A, B>.set(b: B): (S) -> T = modify { b }

/** Modifies the focus with [f] if present. */
fun <S, T, A, B> Optional<S, T, A, B>.modify(f: (A) -> B): (S) -> T =
  Function1.narrow(runOptional(object : Function1Traversing {}, Function1(f))).run

/** Composes with [other], focusing first through this optional and then through [other]. */
infix fun <S, T, A, B, C, D> Optional<S, T, A, B>.andThen(other: Optional<A, B, C, D>): Optional<S, T, C, D> =
  object : Optional<S, T, C, D> {
    override fun <P, F, Q> runOptional(profunctor: Q, pab: K3<P, F, C, D>): K3<P, F, S, T> where Q : Strong<P, F>, Q : Choice<P, F> =
      this@andThen.runOptional(profunctor, other.runOptional(profunctor, pab))
  }

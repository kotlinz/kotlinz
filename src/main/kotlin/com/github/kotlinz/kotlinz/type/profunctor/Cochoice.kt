package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * The dual of [Choice]: a profunctor that can drop a branch of an `Either` threaded through it.
 *
 * Laws:
 * - `unleft(v) == unright(dimap(Either::mirror, Either::mirror, v))`
 * - `unleft(unleft(v)) == unleft(dimap(unassocE, assocE, v))`
 * - For a profunctor that is also [Choice]: `unleft(left(v)) == v`
 *
 * ```kotlin
 * val cochoice = object : ForgetCochoice<Int> {}
 * cochoice.unleft(Forget<Int, Either<String, Int>, Either<String, Int>> { e -> if (e is Either.Left) e.value.length else 0 })("abc") // 3
 * ```
 *
 * @see Choice
 */
interface Cochoice<T, F> : Profunctor<T, F> {
  /** Removes the `Right` branch carried alongside `A` / `B`. */
  fun <A, B, C> unleft(v: K3<T, F, Either<A, C>, Either<B, C>>): K3<T, F, A, B>

  /** Removes the `Left` branch carried alongside `A` / `B`. */
  fun <A, B, C> unright(v: K3<T, F, Either<C, A>, Either<C, B>>): K3<T, F, A, B> =
    unleft<A, B, C>(dimap({ e: Either<A, C> -> e.mirror() }, { e: Either<C, B> -> e.mirror() }, v))
}

private fun <X, Y> Either<X, Y>.mirror(): Either<Y, X> = when (this) {
  is Either.Left -> Either.Right(value)
  is Either.Right -> Either.Left(value)
}

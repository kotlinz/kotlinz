package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * An arrow that can choose between branches based on an `Either` input.
 *
 * Laws:
 * - `left(arr(f)) == arr { e -> e.mapLeft(f) }`
 * - `left(compose(g, f)) == compose(left(g), left(f))`
 * - `compose(left(f), arr(::Left)) == compose(arr(::Left), f)`
 *
 * @see Arrow
 * @see com.github.kotlinz.kotlinz.type.profunctor.Choice
 */
interface ArrowChoice<T, F> : Arrow<T, F> {
    /** Applies `f` to `Left` inputs, passing `Right` inputs through unchanged. */
    fun <A, B, C> left(f: K3<T, F, A, B>): K3<T, F, Either<A, C>, Either<B, C>>

    /** Applies `f` to `Right` inputs, passing `Left` inputs through unchanged. */
    fun <A, B, C> right(f: K3<T, F, A, B>): K3<T, F, Either<C, A>, Either<C, B>> =
            compose(arr(::mirror), compose(left<A, B, C>(f), arr(::mirror)))

    /** Applies `f` to `Left` inputs and `g` to `Right` inputs (`+++`). */
    fun <A, B, C, D> splitChoice(f: K3<T, F, A, B>, g: K3<T, F, C, D>): K3<T, F, Either<A, C>, Either<B, D>> =
            compose(right<C, D, B>(g), left<A, B, C>(f))

    /** Handles both branches and merges them into a single output (`|||`). */
    fun <A, B, C> fanin(f: K3<T, F, A, C>, g: K3<T, F, B, C>): K3<T, F, Either<A, B>, C> =
            compose(arr(::untag), splitChoice(f, g))
}

private fun <X, Y> mirror(e: Either<X, Y>): Either<Y, X> = when (e) {
    is Either.Left -> Either.Right(e.value)
    is Either.Right -> Either.Left(e.value)
}

private fun <X> untag(e: Either<X, X>): X = when (e) {
    is Either.Left -> e.value
    is Either.Right -> e.value
}

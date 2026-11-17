package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

interface ArrowChoice<T, F> : Arrow<T, F> {
    fun <A, B, C> left(f: K3<T, F, A, B>): K3<T, F, Either<A, C>, Either<B, C>>

    fun <A, B, C> right(f: K3<T, F, A, B>): K3<T, F, Either<C, A>, Either<C, B>> =
            compose(arr(::mirror), compose(left<A, B, C>(f), arr(::mirror)))

    fun <A, B, C, D> splitChoice(f: K3<T, F, A, B>, g: K3<T, F, C, D>): K3<T, F, Either<A, C>, Either<B, D>> =
            compose(right<C, D, B>(g), left<A, B, C>(f))

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

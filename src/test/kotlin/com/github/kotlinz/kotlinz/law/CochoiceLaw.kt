package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Cochoice
import io.kotest.matchers.booleans.shouldBeTrue

class CochoiceLaw<T, F>(val cochoice: Cochoice<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()

    fun unrightMirrorsUnleft(v: K3<T, F, Either<Int, Int>, Either<Int, Int>>): Boolean =
            same(cochoice.unright<Int, Int, Int>(v), cochoice.unleft<Int, Int, Int>(cochoice.dimap(::flip, ::flip, v)))

    fun unleftAssociates(v: K3<T, F, Either<Either<Int, Int>, Int>, Either<Either<Int, Int>, Int>>): Boolean =
            same(
                    cochoice.unleft<Int, Int, Int>(cochoice.unleft<Either<Int, Int>, Either<Int, Int>, Int>(v)),
                    cochoice.unleft<Int, Int, Either<Int, Int>>(cochoice.dimap(::unassocEither, ::assocEither, v))
            )

    fun assertSatisfyingCochoiceLaw(
            v: K3<T, F, Either<Int, Int>, Either<Int, Int>>,
            w: K3<T, F, Either<Either<Int, Int>, Int>, Either<Either<Int, Int>, Int>>
    ) {
        unrightMirrorsUnleft(v).shouldBeTrue()
        unleftAssociates(w).shouldBeTrue()
    }

    private fun same(a: K3<T, F, Int, *>, b: K3<T, F, Int, *>): Boolean = ints.all { observe(a)(it) == observe(b)(it) }
}

private fun <X, Y> flip(e: Either<X, Y>): Either<Y, X> = when (e) {
    is Either.Left -> Either.Right(e.value)
    is Either.Right -> Either.Left(e.value)
}

private fun <A, C, D> assocEither(e: Either<Either<A, C>, D>): Either<A, Either<C, D>> = when (e) {
    is Either.Left -> when (val inner = e.value) {
        is Either.Left -> Either.Left(inner.value)
        is Either.Right -> Either.Right(Either.Left(inner.value))
    }
    is Either.Right -> Either.Right(Either.Right(e.value))
}

private fun <B, C, D> unassocEither(e: Either<B, Either<C, D>>): Either<Either<B, C>, D> = when (e) {
    is Either.Left -> Either.Left(Either.Left(e.value))
    is Either.Right -> when (val inner = e.value) {
        is Either.Left -> Either.Left(Either.Right(inner.value))
        is Either.Right -> Either.Right(inner.value)
    }
}

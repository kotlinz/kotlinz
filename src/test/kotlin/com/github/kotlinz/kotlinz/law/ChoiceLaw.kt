package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Choice
import io.kotest.matchers.booleans.shouldBeTrue

class ChoiceLaw<T, F>(val choice: Choice<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()
    private val eithers: List<Either<Int, Int>> = ints.flatMap { listOf(Either.Left<Int, Int>(it), Either.Right<Int, Int>(it)) }

    fun leftInjects(v: K3<T, F, Int, Int>): Boolean =
            same(choice.rmap({ b: Int -> Either.Left<Int, Int>(b) }, v), choice.lmap({ a: Int -> Either.Left<Int, Int>(a) }, choice.left<Int, Int, Int>(v)), ints)

    fun leftCommutes(v: K3<T, F, Int, Int>, f: (Int) -> Int): Boolean =
            same(
                    choice.lmap({ e: Either<Int, Int> -> mapRight(e, f) }, choice.left<Int, Int, Int>(v)),
                    choice.rmap({ e: Either<Int, Int> -> mapRight(e, f) }, choice.left<Int, Int, Int>(v)),
                    eithers
            )

    fun leftAssociates(v: K3<T, F, Int, Int>): Boolean =
            same(
                    choice.left<Either<Int, Int>, Either<Int, Int>, Int>(choice.left<Int, Int, Int>(v)),
                    choice.dimap(::assocE, ::unassocE, choice.left<Int, Int, Either<Int, Int>>(v)),
                    eithers.map { Either.Left<Either<Int, Int>, Int>(it) } + ints.map { Either.Right<Either<Int, Int>, Int>(it) }
            )

    fun rightMirrorsLeft(v: K3<T, F, Int, Int>): Boolean =
            same(choice.right<Int, Int, Int>(v), choice.dimap(::mirror, ::mirror, choice.left<Int, Int, Int>(v)), eithers)

    fun assertSatisfyingChoiceLaw(v: K3<T, F, Int, Int>) {
        leftInjects(v).shouldBeTrue()
        leftCommutes(v) { it * 7 }.shouldBeTrue()
        leftAssociates(v).shouldBeTrue()
        rightMirrorsLeft(v).shouldBeTrue()
    }

    private fun <X> same(a: K3<T, F, X, *>, b: K3<T, F, X, *>, inputs: List<X>): Boolean =
            inputs.all { observe(a)(it) == observe(b)(it) }
}

private fun <A, C, D> assocE(e: Either<Either<A, C>, D>): Either<A, Either<C, D>> = when (e) {
    is Either.Left -> when (val inner = e.value) {
        is Either.Left -> Either.Left(inner.value)
        is Either.Right -> Either.Right(Either.Left(inner.value))
    }
    is Either.Right -> Either.Right(Either.Right(e.value))
}

private fun <B, C, D> unassocE(e: Either<B, Either<C, D>>): Either<Either<B, C>, D> = when (e) {
    is Either.Left -> Either.Left(Either.Left(e.value))
    is Either.Right -> when (val inner = e.value) {
        is Either.Left -> Either.Left(Either.Right(inner.value))
        is Either.Right -> Either.Right(inner.value)
    }
}

private fun <X, Y> mirror(e: Either<X, Y>): Either<Y, X> = when (e) {
    is Either.Left -> Either.Right(e.value)
    is Either.Right -> Either.Left(e.value)
}

private fun <X, Y> mapRight(e: Either<X, Y>, f: (Y) -> Y): Either<X, Y> = when (e) {
    is Either.Left -> e
    is Either.Right -> Either.Right(f(e.value))
}

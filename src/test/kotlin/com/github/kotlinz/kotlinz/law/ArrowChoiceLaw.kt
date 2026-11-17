package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.arrow.ArrowChoice
import io.kotest.matchers.booleans.shouldBeTrue

class ArrowChoiceLaw<T, F>(val arrow: ArrowChoice<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()
    private val eithers: List<Either<Int, Int>> = ints.flatMap { listOf(Either.Left<Int, Int>(it), Either.Right<Int, Int>(it)) }

    fun leftArr(f: (Int) -> Int): Boolean =
            same(arrow.left<Int, Int, Int>(arrow.arr(f)), arrow.arr { e: Either<Int, Int> -> mapLeft(e, f) }, eithers)

    fun leftComposition(v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>): Boolean =
            same(arrow.left<Int, Int, Int>(andThen(v, w)), andThen(arrow.left<Int, Int, Int>(v), arrow.left<Int, Int, Int>(w)), eithers)

    fun leftInjects(v: K3<T, F, Int, Int>): Boolean =
            same(andThen(v, arrow.arr { b: Int -> Either.Left<Int, Int>(b) }), andThen(arrow.arr { a: Int -> Either.Left<Int, Int>(a) }, arrow.left<Int, Int, Int>(v)), ints)

    fun leftCommutes(v: K3<T, F, Int, Int>, g: (Int) -> Int): Boolean {
        val mapRight = arrow.arr { e: Either<Int, Int> -> mapRight(e, g) }
        return same(andThen(arrow.left<Int, Int, Int>(v), mapRight), andThen(mapRight, arrow.left<Int, Int, Int>(v)), eithers)
    }

    fun leftAssociates(v: K3<T, F, Int, Int>): Boolean {
        val assoc = arrow.arr { e: Either<Either<Int, Int>, Int> -> assocSum(e) }
        val inputs = eithers.map { Either.Left<Either<Int, Int>, Int>(it) } + ints.map { Either.Right<Either<Int, Int>, Int>(it) }
        return same(andThen(arrow.left<Either<Int, Int>, Either<Int, Int>, Int>(arrow.left<Int, Int, Int>(v)), assoc), andThen(assoc, arrow.left<Int, Int, Either<Int, Int>>(v)), inputs)
    }

    fun assertSatisfyingArrowChoiceLaw(v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>) {
        leftArr { it + 1 }.shouldBeTrue()
        leftComposition(v, w).shouldBeTrue()
        leftInjects(v).shouldBeTrue()
        leftCommutes(v) { it * 7 }.shouldBeTrue()
        leftAssociates(v).shouldBeTrue()
    }

    private fun <A, B, C> andThen(f: K3<T, F, A, B>, g: K3<T, F, B, C>): K3<T, F, A, C> = arrow.compose(g, f)

    private fun <X> same(a: K3<T, F, X, *>, b: K3<T, F, X, *>, inputs: List<X>): Boolean =
            inputs.all { observe(a)(it) == observe(b)(it) }
}

private fun <X, Y> mapLeft(e: Either<X, Y>, f: (X) -> X): Either<X, Y> = when (e) {
    is Either.Left -> Either.Left(f(e.value))
    is Either.Right -> e
}

private fun <X, Y> mapRight(e: Either<X, Y>, f: (Y) -> Y): Either<X, Y> = when (e) {
    is Either.Left -> e
    is Either.Right -> Either.Right(f(e.value))
}

private fun <A, B, C> assocSum(e: Either<Either<A, B>, C>): Either<A, Either<B, C>> = when (e) {
    is Either.Left -> when (val inner = e.value) {
        is Either.Left -> Either.Left(inner.value)
        is Either.Right -> Either.Right(Either.Left(inner.value))
    }
    is Either.Right -> Either.Right(Either.Right(e.value))
}

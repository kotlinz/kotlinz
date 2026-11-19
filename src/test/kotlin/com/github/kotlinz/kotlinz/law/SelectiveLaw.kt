package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Selective
import io.kotest.matchers.booleans.shouldBeTrue

class SelectiveLaw<T>(val selective: Selective<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A> identity(x: K1<T, Either<A, A>>): Boolean =
            eq(selective.select(x, selective.pure { a: A -> a }), selective.fmap({ e: Either<A, A> -> merge(e) }, x))

    fun <A, B> distributivity(x: Either<A, B>, y: K1<T, (A) -> B>, z: K1<T, (A) -> B>): Boolean {
        val px = selective.pure(x)
        return eq(selective.select(px, andThen(y, z)), andThen(selective.select(px, y), selective.select(px, z)))
    }

    fun <A, B, C> associativity(x: K1<T, Either<B, C>>, y: K1<T, Either<A, (B) -> C>>, z: K1<T, (A) -> (B) -> C>): Boolean {
        val left = selective.select(x, selective.select(y, z))
        val fx = selective.fmap({ e: Either<B, C> -> rightNested<B, Pair<A, B>, C>(e) }, x)
        val gy = selective.fmap({ e: Either<A, (B) -> C> -> { b: B -> pairOrApply(e, b) } }, y)
        val hz = selective.fmap({ f: (A) -> (B) -> C -> { p: Pair<A, B> -> f(p.first)(p.second) } }, z)
        return eq(left, selective.select(selective.select(fx, gy), hz))
    }

    fun <A, B, C> assertSatisfyingSelectiveLaw(
        x: K1<T, Either<B, C>>, y: K1<T, Either<A, (B) -> C>>, z: K1<T, (A) -> (B) -> C>,
        same: K1<T, Either<A, A>>, pureX: Either<A, B>, f: K1<T, (A) -> B>, g: K1<T, (A) -> B>
    ) {
        identity(same).shouldBeTrue()
        distributivity(pureX, f, g).shouldBeTrue()
        associativity(x, y, z).shouldBeTrue()
    }

    private fun <A, B> andThen(a: K1<T, A>, b: K1<T, B>): K1<T, B> =
            selective.ap(selective.fmap({ _: A -> { v: B -> v } }, a), b)

    private fun <A> merge(e: Either<A, A>): A = when (e) {
        is Either.Left -> e.value
        is Either.Right -> e.value
    }

    private fun <A, E, C> rightNested(e: Either<A, C>): Either<A, Either<E, C>> = when (e) {
        is Either.Left -> Either.Left(e.value)
        is Either.Right -> Either.Right(Either.Right(e.value))
    }

    private fun <A, B, C> pairOrApply(e: Either<A, (B) -> C>, b: B): Either<Pair<A, B>, C> = when (e) {
        is Either.Left -> Either.Left(Pair(e.value, b))
        is Either.Right -> Either.Right(e.value(b))
    }
}

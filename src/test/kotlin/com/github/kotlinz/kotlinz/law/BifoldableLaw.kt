package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.monoid.toListMonoid
import com.github.kotlinz.kotlinz.type.Bifoldable
import io.kotest.matchers.booleans.shouldBeTrue

class BifoldableLaw<T>(val bifoldable: Bifoldable<T>) {
    fun <A, B> foldOrderConsistency(v: K2<T, A, B>): Boolean = viaFoldLeft(v) == viaFoldRight(v)

    fun <A, B> bifoldMapConsistency(v: K2<T, A, B>): Boolean = viaFoldMap(v) == viaFoldLeft(v)

    fun <A, B> assertSatisfyingBifoldableLaw(v: K2<T, A, B>) {
        foldOrderConsistency(v).shouldBeTrue()
        bifoldMapConsistency(v).shouldBeTrue()
    }

    private fun <A, B> viaFoldLeft(v: K2<T, A, B>): List<Either<A, B>> =
            bifoldable.bifoldLeft(listOf<Either<A, B>>(), { acc, a: A -> acc + Either.Left(a) }, { acc, b: B -> acc + Either.Right(b) }, v)

    private fun <A, B> viaFoldRight(v: K2<T, A, B>): List<Either<A, B>> =
            bifoldable.bifoldRight(listOf<Either<A, B>>(), { a: A, acc -> listOf(Either.Left<A, B>(a)) + acc }, { b: B, acc -> listOf(Either.Right<A, B>(b)) + acc }, v)

    private fun <A, B> viaFoldMap(v: K2<T, A, B>): List<*> =
            bifoldable.bifoldMap(ListMonoid::class.java, { a: A -> listOf(Either.Left<A, B>(a)).toListMonoid() }, { b: B -> listOf(Either.Right<A, B>(b)).toListMonoid() }, v).value
}

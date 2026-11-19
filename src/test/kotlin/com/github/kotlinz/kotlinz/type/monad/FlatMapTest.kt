package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.FlatMapLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <T> halveTwice(flatMap: FlatMap<T>, half: (Int) -> K1<T, Int>, v: K1<T, Int>): K1<T, Int> =
    flatMap.bind(half, flatMap.bind(half, v))

class FlatMapTest : DescribeSpec({
    describe("FlatMap") {
        val half = { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }

        it("should be a supertype of Monad usable without pure") {
            val flatMap: FlatMap<Maybe.T> = object : MaybeMonad {}
            halveTwice(flatMap, half, Maybe.Just(8)) shouldBe Maybe.Just(2)
            halveTwice(flatMap, half, Maybe.Just(6)) shouldBe Maybe.None<Int>()
            flatMap.join(Maybe.Just(Maybe.Just(1))) shouldBe Maybe.Just(1)
        }

        it("should satisfy FlatMap laws for Maybe") {
            FlatMapLaw(object : MaybeMonad {}).assertSatisfyingFlatMapLaw(
                Maybe.Just(8), half, { n: Int -> Maybe.Just(n.toString()) }, Maybe.Just { n: Int -> n + 1 })
        }

        it("should satisfy FlatMap laws for ListK") {
            FlatMapLaw(object : ListKMonad {}).assertSatisfyingFlatMapLaw(
                ListK(listOf(1, 2)), { n: Int -> ListK(listOf(n, n * 10)) }, { n: Int -> ListK(listOf(n.toString())) },
                ListK(listOf({ n: Int -> n + 1 }, { n: Int -> n * 2 })))
        }
    }
})

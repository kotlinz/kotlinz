package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFoldable
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <A> listOfMaybe(vararg values: Maybe<A>): Compose<ListK.T, Maybe.T, A> =
    Compose(ListK(values.toList<K1<Maybe.T, A>>()))

class ComposeTest : DescribeSpec({
    describe("Compose") {
        val listK = object : ListKMonad {}
        val maybe = object : MaybeMonad {}
        val compose = listOfMaybe(Maybe.Just(1), Maybe.None(), Maybe.Just(3))

        context("as Functor") {
            val law = FunctorLaw(Compose.functor(listK, maybe))

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(compose, { it + 1 }, { it * 2 }) }
            it("should map the inner values") {
                Compose.functor(listK, maybe).fmap({ x: Int -> x * 10 }, compose) shouldBe listOfMaybe(Maybe.Just(10), Maybe.None(), Maybe.Just(30))
            }
        }

        context("as Applicative") {
            val applicative = Compose.applicativeOps(listK, maybe)
            val u = listOfMaybe<(String) -> Int>(Maybe.Just { it.length }, Maybe.None())
            val v = listOfMaybe<(Int) -> String>(Maybe.Just { it.toString() }, Maybe.Just { "#$it" })

            it("should satisfy laws") { ApplicativeLaw(applicative).assertSatisfyingApplicativeLaw(compose, 1, u, v) { it.toString() } }

            it("should combine both layers") {
                val add = applicative.liftA2 { a: Int, b: Int -> a + b }
                add(listOfMaybe(Maybe.Just(1), Maybe.Just(2)), listOfMaybe(Maybe.Just(10), Maybe.None())) shouldBe
                    listOfMaybe(Maybe.Just(11), Maybe.None(), Maybe.Just(12), Maybe.None())
            }

            it("should lift values with pure") {
                Compose.pure(listK, maybe, 5) shouldBe listOfMaybe(Maybe.Just(5))
            }
        }

        context("as Foldable") {
            val foldable = Compose.foldable(object : ListKFoldable {}, object : MaybeFoldable {})

            it("should satisfy laws") { FoldableLaw(foldable).assertSatisfyingFoldableLaw(compose) }
            it("should fold through both layers") { foldable.foldr("", { a: Int, acc: String -> "$a$acc" }, compose) shouldBe "13" }
        }
    }
})

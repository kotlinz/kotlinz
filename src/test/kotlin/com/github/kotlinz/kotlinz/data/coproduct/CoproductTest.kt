package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFoldable
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.law.FoldableLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CoproductTest : DescribeSpec({
    describe("Coproduct") {
        val left = Coproduct.left<ListK.T, Maybe.T, Int>(ListK(listOf(1, 2)))
        val right = Coproduct.right<ListK.T, Maybe.T, Int>(Maybe.Just(3))

        context("as Functor") {
            val functor = Coproduct.functor(object : ListKFunctor {}, object : MaybeFunctor {})
            val law = FunctorLaw(functor)

            it("should satisfy laws with left") { law.assertSatisfyingFunctorLaw(left, { it + 1 }, { it * 2 }) }
            it("should satisfy laws with right") { law.assertSatisfyingFunctorLaw(right, { it + 1 }, { it * 2 }) }

            it("should map whichever side is present") {
                functor.fmap({ x: Int -> x * 10 }, left) shouldBe Coproduct.left<ListK.T, Maybe.T, Int>(ListK(listOf(10, 20)))
                functor.fmap({ x: Int -> x * 10 }, right) shouldBe Coproduct.right<ListK.T, Maybe.T, Int>(Maybe.Just(30))
            }
        }

        context("as Foldable") {
            val foldable = Coproduct.foldable(object : ListKFoldable {}, object : MaybeFoldable {})

            it("should satisfy laws") {
                FoldableLaw(foldable).assertSatisfyingFoldableLaw(left)
                FoldableLaw(foldable).assertSatisfyingFoldableLaw(right)
            }

            it("should fold the present side") {
                foldable.toList(left) shouldBe listOf(1, 2)
                foldable.toList(right) shouldBe listOf(3)
            }
        }

        context("fold") {
            it("should eliminate either side") {
                left.fold({ "list" }, { "maybe" }) shouldBe "list"
                right.fold({ "list" }, { "maybe" }) shouldBe "maybe"
            }
        }
    }
})

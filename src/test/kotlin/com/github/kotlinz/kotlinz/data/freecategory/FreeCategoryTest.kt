package com.github.kotlinz.kotlinz.data.freecategory

import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Category
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val function1 = object : Function1Category {}

private fun <A, B> run(path: FreeCategory<Step.T, A, B>): Function1<A, B> = Function1.narrow(path.foldMap(function1, StepToFunction1))

private fun same(a: FreeCategory<Step.T, Int, Int>, b: FreeCategory<Step.T, Int, Int>): Boolean =
    (-3..3).all { run(a)(it) == run(b)(it) }

class FreeCategoryTest : DescribeSpec({
    describe("FreeCategory") {
        val category = object : FreeCategoryCategory<Step.T> {}
        val inc = FreeCategory.lift(Step.Inc)
        val double = FreeCategory.lift(Step.Double)

        context("as Category") {
            it("should satisfy identity laws") {
                same(category.compose(category.id(), inc), inc) shouldBe true
                same(category.compose(inc, category.id()), inc) shouldBe true
            }

            it("should satisfy associativity") {
                same(category.compose(inc, category.compose(double, inc)), category.compose(category.compose(inc, double), inc)) shouldBe true
            }
        }

        context("interpretation") {
            it("should run a pipeline as functions") {
                val pipeline = FreeCategory.lift(Step.Parse) andThen double andThen inc andThen FreeCategory.lift(Step.Show)
                run(pipeline)("20") shouldBe "41"
            }

            it("should compose right to left") { run(inc compose double)(5) shouldBe 11 }
            it("should interpret the empty path as identity") { run(FreeCategory.id<Step.T, String>())("x") shouldBe "x" }

            it("should support different interpreters for the same description") {
                val pipeline = inc andThen double andThen inc
                val log = Function1.narrow(pipeline.foldMap(function1, StepNames))
                @Suppress("UNCHECKED_CAST")
                (log as Function1<List<String>, List<String>>)(emptyList()) shouldBe listOf("Inc", "Double", "Inc")
            }
        }
    }
})

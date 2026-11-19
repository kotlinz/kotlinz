package com.github.kotlinz.kotlinz.data.freearrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.freecategory.Step
import com.github.kotlinz.kotlinz.data.freecategory.StepToFunction1
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Arrow
import com.github.kotlinz.kotlinz.law.ArrowLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val function1 = object : Function1Arrow {}

private fun <A, B> run(free: FreeArrow<Step.T, A, B>): Function1<A, B> = Function1.narrow(free.foldMap(function1, StepToFunction1))

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<FreeArrow.T, Step.T, *, *>): (Any?) -> Any? = run(v as FreeArrow<Step.T, Any?, Any?>).run

class FreeArrowTest : DescribeSpec({
    describe("FreeArrow") {
        val inc = FreeArrow.lift(Step.Inc)
        val double = FreeArrow.lift(Step.Double)

        context("as Category and Arrow") {
            val law = ArrowLaw(object : FreeArrowArrow<Step.T> {}, ::observe)

            it("should satisfy category laws") { law.assertSatisfyingCategoryLaw(inc, double, inc) }
            it("should satisfy arrow laws") { law.assertSatisfyingArrowLaw(inc, double) }
        }

        context("interpretation") {
            it("should mix plain functions and quiver arrows") {
                run(FreeArrow.lift(Step.Parse) andThen FreeArrow.arr { n: Int -> n - 1 } andThen double)("4") shouldBe 6
                run(inc compose double)(5) shouldBe 11
                run(FreeArrow.id<Step.T, String>())("x") shouldBe "x"
            }

            it("should route pairs") {
                run(inc.first<String>())(Pair(1, "a")) shouldBe Pair(2, "a")
                run(inc.second<String>())(Pair("a", 1)) shouldBe Pair("a", 2)
                run(double split FreeArrow.lift(Step.Show))(Pair(2, 7)) shouldBe Pair(4, "7")
                run(inc combine double)(5) shouldBe Pair(6, 10)
            }
        }
    }
})

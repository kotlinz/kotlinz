package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListComonad
import com.github.kotlinz.kotlinz.law.ComonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val nel = object : NonEmptyListComonad {}

private fun <A> NonEmptyList<A>.all(): List<A> = listOf(head) + tail

@Suppress("UNCHECKED_CAST")
private fun densityEq(a: Any?, b: Any?): Boolean {
    if (a !is Density<*, *> || b !is Density<*, *>) return a == b
    val x = NonEmptyList.narrow((a as Density<NonEmptyList.T, Any?>).lower(nel)).all()
    val y = NonEmptyList.narrow((b as Density<NonEmptyList.T, Any?>).lower(nel)).all()
    return x.size == y.size && x.zip(y).all { (l, r) -> densityEq(l, r) }
}

class DensityTest : DescribeSpec({
    describe("Density") {
        val list = NonEmptyList(1, listOf(2, 3))
        val density = Density.lift(nel, list)

        context("as Comonad") {
            val law = ComonadLaw(object : DensityComonad<NonEmptyList.T> {}, ::densityEq)

            it("should satisfy laws") { law.assertSatisfyingComonadLaw(density) }
            it("should satisfy laws after fmap") { law.assertSatisfyingComonadLaw(density fmap { it.toString() }) }

            it("should satisfy laws with a custom observer") {
                val sum = Density.of({ w: K1<NonEmptyList.T, Int> -> NonEmptyList.narrow(w).all().sum() }, list)
                law.assertSatisfyingComonadLaw(sum)
            }
        }

        context("lift and lower") {
            it("should round-trip through lift and lower") { density.lower(nel) shouldBe list }
            it("should extract the focus") { density.extract() shouldBe 1 }

            it("should agree with the underlying comonad") {
                val sum = { w: K1<K1<Density.T, NonEmptyList.T>, Int> -> NonEmptyList.narrow(Density.narrow(w).lower(nel)).all().sum() }
                (density extend sum).lower(nel) shouldBe NonEmptyList(6, listOf(5, 3))
            }
        }
    }
})

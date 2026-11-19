package com.github.kotlinz.kotlinz.data.cokleisli

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListComonad
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.law.ArrowLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val nelComonad = object : NonEmptyListComonad {}

private fun nel(w: K1<NonEmptyList.T, Int>): NonEmptyList<Int> = NonEmptyList.narrow(w)

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Cokleisli.T, NonEmptyList.T, *, *>): (Any?) -> Any? =
    { x -> (v as Cokleisli<NonEmptyList.T, Any?, Any?>).run(NonEmptyList(x, listOf(x, x))) }

private fun sameCokleisli(a: K3<Cokleisli.T, NonEmptyList.T, Int, Int>, b: K3<Cokleisli.T, NonEmptyList.T, Int, Int>): Boolean =
    listOf(nonEmptyListOf(1), nonEmptyListOf(3, -1, 4), nonEmptyListOf(0, 2)).all { Cokleisli.narrow(a).run(it) == Cokleisli.narrow(b).run(it) }

class CokleisliTest : DescribeSpec({
    describe("Cokleisli") {
        val total = Cokleisli(nelComonad) { w: K1<NonEmptyList.T, Int> -> nel(w).let { it.head + it.tail.sum() } }
        val weighted = Cokleisli(nelComonad) { w: K1<NonEmptyList.T, Int> -> nel(w).let { it.head * 2 + it.tail.size * 10 } }

        context("as Category and Arrow") {
            val law = ArrowLaw(object : CokleisliArrow<NonEmptyList.T> { override val comonad = nelComonad }, ::observe)

            it("should satisfy category laws") { law.assertSatisfyingCategoryLaw(total, weighted, total) }
            it("should satisfy arrow laws") { law.assertSatisfyingArrowLaw(total, weighted) }

            it("should compose by extending over the context") {
                (total andThen weighted).run(nonEmptyListOf(1, 2, 3)) shouldBe 32
                (weighted compose total).run(nonEmptyListOf(1, 2, 3)) shouldBe 32
                Cokleisli.id<NonEmptyList.T, Int>(nelComonad).run(nonEmptyListOf(7, 8)) shouldBe 7
                Cokleisli.arr(nelComonad) { a: Int -> a + 1 }.run(nonEmptyListOf(7, 8)) shouldBe 8
            }

            it("should route pairs") {
                val pairs = nonEmptyListOf(Pair(1, "a"), Pair(2, "b"))
                total.first<String>().run(pairs) shouldBe Pair(3, "a")
                total.second<String>().run(nonEmptyListOf(Pair("a", 1), Pair("b", 2))) shouldBe Pair("a", 3)
                (total split weighted).run(nonEmptyListOf(Pair(1, 5), Pair(2, 6))) shouldBe Pair(3, 20)
                (total combine weighted).run(nonEmptyListOf(1, 2)) shouldBe Pair(3, 12)
            }
        }

        context("as Profunctor") {
            val law = ProfunctorLaw(object : CokleisliProfunctor<NonEmptyList.T> { override val comonad = nelComonad }, ::sameCokleisli)

            it("should satisfy laws") { law.assertSatisfyingProfunctorLaw(total) }

            it("should map the context and the result") {
                (total lmap { s: String -> s.length }).run(nonEmptyListOf("ab", "c")) shouldBe 3
                (total rmap { "n=$it" }).run(nonEmptyListOf(1, 2)) shouldBe "n=3"
                total.dimap({ s: String -> s.length }, { it * 2 })(nonEmptyListOf("abc")) shouldBe 6
            }
        }
    }
})

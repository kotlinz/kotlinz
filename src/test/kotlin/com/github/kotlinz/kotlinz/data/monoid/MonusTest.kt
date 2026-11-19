package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonusLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Monus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MonusTest : DescribeSpec({
    describe("Natural Monoid") {
        val clazz = NaturalMonoid::class.java
        it("should satisfy laws") {
            MonusLaw(clazz).assertSatisfyingMonusLaw(5L.toNaturalMonoid(), 3L.toNaturalMonoid(), 0L.toNaturalMonoid())
            MonusLaw(clazz).assertSatisfyingMonusLaw(1L.toNaturalMonoid(), 7L.toNaturalMonoid(), 4L.toNaturalMonoid())
        }
        it("should truncate subtraction at zero") {
            Monus.monus(clazz, 5L.toNaturalMonoid(), 3L.toNaturalMonoid()) shouldBe 2L.toNaturalMonoid()
            Monus.monus(clazz, 3L.toNaturalMonoid(), 5L.toNaturalMonoid()) shouldBe 0L.toNaturalMonoid()
        }
        it("should add values") { Monoid.op(clazz, 2L.toNaturalMonoid(), 3L.toNaturalMonoid()) shouldBe 5L.toNaturalMonoid() }
        it("should reject negative values") { shouldThrow<IllegalArgumentException> { (-1L).toNaturalMonoid() } }
        it("should reject overflow") {
            shouldThrow<ArithmeticException> { Monoid.op(clazz, Long.MAX_VALUE.toNaturalMonoid(), 1L.toNaturalMonoid()) }
        }
    }

    describe("Multiset Monoid") {
        val clazz = MultisetMonoid::class.java as Class<MultisetMonoid<String>>
        val aab = listOf("a", "a", "b").toMultisetMonoid()
        val ac = listOf("a", "c").toMultisetMonoid()
        it("should satisfy laws") {
            MonusLaw(clazz).assertSatisfyingMonusLaw(aab, ac, listOf<String>().toMultisetMonoid())
            MonusLaw(clazz).assertSatisfyingMonusLaw(aab, listOf("b", "b").toMultisetMonoid(), ac)
        }
        it("should add multiplicities") { Monoid.op(clazz, aab, ac) shouldBe listOf("a", "a", "a", "b", "c").toMultisetMonoid() }
        it("should subtract multiplicities and drop missing elements") {
            Monus.monus(clazz, aab, ac) shouldBe listOf("a", "b").toMultisetMonoid()
            Monus.monus(clazz, ac, aab) shouldBe listOf("c").toMultisetMonoid()
            Monus.monus(clazz, aab, ac).count("c") shouldBe 0
        }
        it("should reject non-positive counts") { shouldThrow<IllegalArgumentException> { MultisetMonoid(mapOf("a" to 0)) } }
    }

    describe("Set Monoid") {
        val clazz = SetMonoid::class.java as Class<SetMonoid<Int>>
        it("should satisfy laws") {
            MonusLaw(clazz).assertSatisfyingMonusLaw(setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid(), setOf(3).toSetMonoid())
        }
        it("should use set difference") {
            Monus.monus(clazz, setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid()) shouldBe setOf(1).toSetMonoid()
        }
    }
})

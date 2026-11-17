package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.BifunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WriterBifunctorTest : DescribeSpec({
    describe("Writer") {
        context("as Bifunctor") {
            val law = BifunctorLaw(object : WriterBifunctor {}) { a, b -> runOf(a) == runOf(b) }
            val writer = Writer(StringMonoid::class.java, Pair(1, "log".toStringMonoid()))
            val f = { s: StringMonoid -> (s.value + "!").toStringMonoid() }
            val g = { s: StringMonoid -> s.value.uppercase().toStringMonoid() }
            val h = { i: Int -> i + 1 }
            val i = { i: Int -> i * 2 }

            it("should satisfy laws") { law.assertSatisfyingBifunctorLaw(writer, f, g, h, i) }

            it("should map both the log and the value") {
                writer.bimap(g, h).run shouldBe Pair(2, "LOG".toStringMonoid())
            }

            it("should change the log type") {
                val mapped = writer first { s -> ListMonoid(listOf(s.value)) }
                mapped.type shouldBe ListMonoid::class.java
                (mapped bind { a -> Writer(ListMonoid::class.java, Pair(a, ListMonoid(listOf("next")))) }).run shouldBe
                        Pair(1, ListMonoid(listOf("log", "next")))
            }
        }
    }
})

private fun runOf(v: K2<Writer.T, *, *>) = (v as Writer<*, *>).run

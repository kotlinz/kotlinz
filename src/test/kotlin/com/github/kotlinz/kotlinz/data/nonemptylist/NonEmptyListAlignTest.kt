package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NonEmptyListAlignTest : DescribeSpec({
    describe("NonEmptyList") {
        val xs = nonEmptyListOf(1, 2, 3)
        val ys = nonEmptyListOf(10)
        val zs = nonEmptyListOf(100, 200)

        context("as Semialign") {
            val law = SemialignLaw(object : NonEmptyListSemialign {})

            it("should satisfy laws") {
                law.assertSatisfyingSemialignLaw(xs, ys, zs, { it + 1 }, { it * 2 })
                law.assertSatisfyingSemialignLaw(ys, zs, xs, { it + 1 }, { it * 2 })
            }

            it("should keep the leftover elements of the longer list") {
                (nonEmptyListOf(1, 2) align nonEmptyListOf("a")) shouldBe nonEmptyListOf(Ior.both(1, "a"), Ior.left(2))
                (nonEmptyListOf(1) align nonEmptyListOf("a", "b")) shouldBe nonEmptyListOf(Ior.both(1, "a"), Ior.right("b"))
            }

            it("should alignWith a function") {
                ys.alignWith(zs) { it is Ior.Both } shouldBe nonEmptyListOf(true, false)
            }
        }

        context("as Zip") {
            val law = ZipLaw(object : NonEmptyListZip {})

            it("should satisfy laws") {
                law.assertSatisfyingZipLaw(xs, ys, zs)
                law.assertSatisfyingZipLaw(zs, xs, ys)
            }

            it("should truncate to the shorter list") {
                (xs zip nonEmptyListOf("a", "b")) shouldBe nonEmptyListOf(Pair(1, "a"), Pair(2, "b"))
                xs.zipWith(zs) { a, b -> a + b } shouldBe nonEmptyListOf(101, 202)
            }
        }
    }
})

package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.law.AlignLaw
import com.github.kotlinz.kotlinz.law.SemialignLaw
import com.github.kotlinz.kotlinz.law.ZipLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKAlignTest : DescribeSpec({
    describe("ListK") {
        val xs = listKOf(1, 2, 3)
        val ys = listKOf(10)
        val zs = listKOf(100, 200)
        val empty = listKOf<Int>()

        context("as Semialign") {
            val law = SemialignLaw(object : ListKSemialign {})

            it("should satisfy laws") {
                law.assertSatisfyingSemialignLaw(xs, ys, zs, { it + 1 }, { it * 2 })
                law.assertSatisfyingSemialignLaw(empty, xs, ys, { it + 1 }, { it * 2 })
            }

            it("should keep the leftover elements of the longer list") {
                (listKOf(1, 2) align listKOf("a")) shouldBe listKOf(Ior.both(1, "a"), Ior.left(2))
                (listKOf(1) align listKOf("a", "b")) shouldBe listKOf(Ior.both(1, "a"), Ior.right("b"))
            }

            it("should alignWith a function") {
                listKOf(1, 2).alignWith(listKOf(10)) { i -> if (i is Ior.Both) i.left + i.right else -1 } shouldBe listKOf(11, -1)
            }
        }

        context("as Align") {
            val law = AlignLaw(object : ListKAlign {})

            it("should satisfy laws") {
                law.assertSatisfyingAlignLaw(xs)
                law.assertSatisfyingAlignLaw(empty)
            }

            it("should expose nil") { ListK.nil<Int>() shouldBe empty }
        }

        context("as Zip") {
            val law = ZipLaw(object : ListKZip {})

            it("should satisfy laws") {
                law.assertSatisfyingZipLaw(xs, ys, zs)
                law.assertSatisfyingZipLaw(empty, xs, ys)
            }

            it("should truncate to the shorter list") {
                (listKOf(1, 2) zip listKOf("a")) shouldBe listKOf(Pair(1, "a"))
                xs.zipWith(zs) { a, b -> a + b } shouldBe listKOf(101, 202)
            }
        }
    }
})

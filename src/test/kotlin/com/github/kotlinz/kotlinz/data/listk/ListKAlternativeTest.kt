package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.AlternativeLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKAlternativeTest : DescribeSpec({
    describe("ListK") {
        context("as Alternative") {
            it("should satisfy laws") {
                AlternativeLaw(object : ListKAlternative {}).assertSatisfyingAlternativeLaw(listKOf(1), listKOf(2, 3), listKOf())
            }

            it("should satisfy laws through MonadPlus") {
                AlternativeLaw(object : ListKMonadPlus {}).assertSatisfyingAlternativeLaw(listKOf(1), listKOf(), listKOf(4))
            }

            it("should concatenate lists") {
                listKOf(1, 2) orElse listKOf(3) shouldBe listKOf(1, 2, 3)
                listKOf(1) orElse ListK.empty() shouldBe listKOf(1)
            }

            it("should make values optional") {
                listKOf(1, 2).optional() shouldBe listKOf(Maybe.Just(1), Maybe.Just(2), Maybe.None())
                listKOf<Int>().optional() shouldBe listKOf(Maybe.None<Int>())
            }
        }

        context("as MonadPlus") {
            it("should treat mzero as left zero of bind") {
                ListK.mzero<Int>() bind { listKOf(it, it) } shouldBe listKOf()
            }
        }
    }
})

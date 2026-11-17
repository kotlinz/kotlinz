package com.github.kotlinz.kotlinz.data.yoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private class CountingListKFunctor : ListKFunctor {
    var calls = 0

    override fun <A, B> fmap(f: (A) -> B, v: K1<ListK.T, A>): ListK<B> {
        calls += 1
        return super.fmap(f, v)
    }
}

class YonedaTest : DescribeSpec({
    describe("Yoneda") {
        val listFunctor = object : ListKFunctor {}
        val list = listKOf(1, 2, 3)
        val yoneda = Yoneda.lift(listFunctor, list)

        context("as Functor") {
            val law = FunctorLaw(object : YonedaFunctor<ListK.T> {}) { a, b -> Yoneda.narrow(a).lower() == Yoneda.narrow(b).lower() }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(yoneda, { it + 1 }, { it.toString() }) }
        }

        context("isomorphism") {
            it("should round-trip lower after lift") { yoneda.lower() shouldBe list }

            it("should round-trip lift after lower") {
                val mapped = yoneda fmap { it * 2 }
                val f = { x: Int -> "$x!" }
                Yoneda.lift(listFunctor, mapped.lower()).run(f) shouldBe mapped.run(f)
            }
        }

        context("fusion") {
            it("should fuse consecutive fmaps into a single fmap of the underlying functor") {
                val counting = CountingListKFunctor()
                val result = (Yoneda.lift(counting, list) fmap { it + 1 } fmap { it * 10 } fmap { it.toString() }).lower()
                result shouldBe listKOf("20", "30", "40")
                counting.calls shouldBe 1
            }
        }
    }
})

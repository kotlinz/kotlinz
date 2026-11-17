package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.ComonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val positions = listOf(-3, 0, 1, 7)

@Suppress("UNCHECKED_CAST")
private fun storeEq(a: Any?, b: Any?): Boolean {
    if (a !is Store<*, *> || b !is Store<*, *>) return a == b
    val x = a as Store<Int, Any?>
    val y = b as Store<Int, Any?>
    return x.pos == y.pos && positions.all { storeEq(x.peek(it), y.peek(it)) }
}

class StoreTest : DescribeSpec({
    describe("Store") {
        val store = Store({ s: Int -> s * 10 }, 2)

        context("as Comonad") {
            val law = ComonadLaw(object : StoreComonad<Int> {}, ::storeEq)

            it("should satisfy laws") { law.assertSatisfyingComonadLaw(store) }
            it("should satisfy laws with String values") { law.assertSatisfyingComonadLaw(store fmap { it.toString() }) }
        }

        context("operations") {
            it("should extract the value at the current position") { store.extract() shouldBe 20 }
            it("should peek at another position") { store.peek(5) shouldBe 50 }
            it("should peek relative to the current position") { store.peeks { it + 1 } shouldBe 30 }
            it("should seek to another position") { store.seek(4).extract() shouldBe 40 }
            it("should seek relative to the current position") { store.seeks { it - 1 }.extract() shouldBe 10 }

            it("should experiment with several positions") {
                val result = store.experiment(object : ListKFunctor {}) { s -> listKOf(s - 1, s, s + 1) }
                ListK.narrow(result) shouldBe listKOf(10, 20, 30)
            }

            it("should extend with a neighbourhood computation") {
                val sum = store extend { w -> Store.narrow(w).let { it.peeks { s -> s - 1 } + it.peeks { s -> s + 1 } } }
                sum.extract() shouldBe 40
                sum.seek(5).extract() shouldBe 100
            }
        }
    }
})

package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.ComonadStoreLaw
import com.github.kotlinz.kotlinz.type.monad.ComonadStore
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val positions = listOf(-3, 0, 1, 7)

@Suppress("UNCHECKED_CAST")
private fun storeEq(a: Any?, b: Any?): Boolean {
    if (a !is Store<*, *> || b !is Store<*, *>) return a == b
    val (x, y) = Pair(a as Store<Int, Any?>, b as Store<Int, Any?>)
    return x.pos == y.pos && positions.all { storeEq(x.peek(it), y.peek(it)) }
}

class StoreComonadStoreTest : DescribeSpec({
    describe("Store as ComonadStore") {
        val instance = object : StoreComonadStore<Int> {}
        val store = Store({ s: Int -> s * 10 }, 2)

        it("should satisfy laws") { ComonadStoreLaw(instance, ::storeEq).assertSatisfyingComonadStoreLaw(5, store) }

        it("should expose position and lookups") {
            instance.pos(store) shouldBe 2
            instance.peek(4, store) shouldBe 40
            instance.peeks({ it + 1 }, store) shouldBe 30
        }

        it("should move with seek and seeks") {
            instance.extract(instance.seek(4, store)) shouldBe 40
            instance.extract(instance.seeks({ it - 1 }, store)) shouldBe 10
        }

        it("should experiment with several positions") {
            val result = instance.experiment(object : ListKFunctor {}, { s: Int -> listKOf(s - 1, s + 1) }, store)
            ListK.narrow(result) shouldBe listKOf(10, 30)
        }

        it("should agree with the default seek derived from duplicate") {
            val defaults = object : ComonadStore<K1<Store.T, Int>, Int>, StoreComonad<Int> {
                override fun <A> pos(v: K1<K1<Store.T, Int>, A>): Int = instance.pos(v)
                override fun <A> peek(s: Int, v: K1<K1<Store.T, Int>, A>): A = instance.peek(s, v)
            }
            storeEq(defaults.seek(7, store), instance.seek(7, store)) shouldBe true
            ComonadStoreLaw(defaults, ::storeEq).assertSatisfyingComonadStoreLaw(-3, store)
        }
    }
})

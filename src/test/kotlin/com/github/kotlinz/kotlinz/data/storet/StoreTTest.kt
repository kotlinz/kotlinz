package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.ComonadStoreLaw
import com.github.kotlinz.kotlinz.law.ComonadTransLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias Base = K1<Env.T, String>

private val positions = listOf(-3, 0, 1, 7)

@Suppress("UNCHECKED_CAST")
private fun sample(storeT: StoreT<*, *, *>): Env<*, List<Any?>> =
    (storeT.run as Env<String, (Int) -> Any?>).let { Env(it.env, positions.map(it.value)) }

private fun storeTEq(a: Any?, b: Any?): Boolean = when {
    a is StoreT<*, *, *> && b is StoreT<*, *, *> -> a.pos == b.pos && storeTEq(sample(a), sample(b))
    a is Env<*, *> && b is Env<*, *> -> a.env == b.env && storeTEq(a.value, b.value)
    a is List<*> && b is List<*> -> a.size == b.size && a.zip(b).all { (x, y) -> storeTEq(x, y) }
    else -> a == b
}

class StoreTTest : DescribeSpec({
    describe("StoreT") {
        val base = object : EnvComonad<String> {}
        val instance = object : StoreTComonadTrans<Int, Base>, StoreTComonadStore<Int, Base> { override val comonad = base }
        val storeT = StoreT(base, Env("ctx") { s: Int -> s * 10 }, 2)

        context("as Comonad") {
            val law = ComonadLaw(instance, ::storeTEq)
            it("should satisfy laws") { law.assertSatisfyingComonadLaw(storeT) }
            it("should satisfy laws with String values") { law.assertSatisfyingComonadLaw(storeT fmap { "v$it" }) }
        }

        context("as ComonadTrans") {
            it("should satisfy laws") {
                ComonadTransLaw(base, instance, ::storeTEq).assertSatisfyingComonadTransLaw(storeT) { w -> Env.narrow(w).let { "${it.env}:${it.value}" } }
            }
        }

        context("as ComonadStore") {
            it("should satisfy laws") { ComonadStoreLaw(instance, ::storeTEq).assertSatisfyingComonadStoreLaw(5, storeT) }
        }

        context("operations") {
            val identity = object : IdentityComonad {}
            val simple = StoreT(identity, Identity { s: Int -> s * 10 }, 2)

            it("should extract the value at the current position") { simple.extract() shouldBe 20 }
            it("should peek at other positions") { simple.peek(5) shouldBe 50 }
            it("should peek relative to the current position") { simple.peeks { it + 1 } shouldBe 30 }
            it("should seek to other positions") { simple.seek(4).extract() shouldBe 40 }
            it("should seek relative to the current position") { simple.seeks { it - 1 }.extract() shouldBe 10 }
            it("should lower to the base comonad") { simple.lower() shouldBe Identity(20) }

            it("should experiment with several positions") {
                ListK.narrow(simple.experiment(object : ListKFunctor {}) { s -> listKOf(s - 1, s + 1) }) shouldBe listKOf(10, 30)
            }

            it("should extend using neighbouring positions") {
                val sum = simple extend { w -> StoreT.narrow(w).let { it.peeks { s -> s - 1 } + it.peeks { s -> s + 1 } } }
                sum.extract() shouldBe 40
                sum.peek(5) shouldBe 100
            }
        }
    }
})

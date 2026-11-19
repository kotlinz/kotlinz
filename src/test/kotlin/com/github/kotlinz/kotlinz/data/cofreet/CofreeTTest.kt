package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.law.ComonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

private typealias Base = K1<Env.T, String>

private fun cofreeTEq(a: Any?, b: Any?, depth: Int = 4): Boolean = when {
    depth == 0 -> true
    a is CofreeT<*, *, *> && b is CofreeT<*, *, *> -> cofreeTEq(a.run, b.run, depth)
    a is Env<*, *> && b is Env<*, *> -> a.env == b.env && cofreeTEq(a.value, b.value, depth)
    a is CofreeF<*, *, *> && b is CofreeF<*, *, *> -> cofreeTEq(a.head, b.head, depth - 1) && cofreeTEq(a.tail, b.tail, depth - 1)
    a is Maybe.Just<*> && b is Maybe.Just<*> -> cofreeTEq(a.value, b.value, depth)
    a is Identity<*> && b is Identity<*> -> cofreeTEq(a.value, b.value, depth)
    else -> a == b
}

private val maybe = object : MaybeFunctor {}
private val env = object : EnvComonad<String> {}

// Counts up from the seed to 3 in a Maybe-branching list, each node keeping the Env context
private fun countUp(seed: Int): CofreeT<Maybe.T, Base, Int> = CofreeT.coiterT(maybe, env, Env("ctx", seed)) { w ->
    Env.narrow(w).let { if (it.value < 3) Maybe.Just(Env("ctx", it.value + 1)) else Maybe.None() }
}

private fun <A> toList(c: CofreeT<Maybe.T, Base, A>): List<A> =
    listOf(c.extract()) + Maybe.narrow(c.unwrap()).let { if (it is Maybe.Just) toList(it.value) else emptyList() }

class CofreeTTest : DescribeSpec({
    describe("CofreeT") {
        val instance = object : CofreeTComonadTrans<Maybe.T, Base> {
            override val functor = maybe
            override val comonad = env
        }
        val list = countUp(0)

        context("as Comonad") {
            val law = ComonadLaw(instance) { a, b -> cofreeTEq(a, b) }
            it("should satisfy laws") { law.assertSatisfyingComonadLaw(list) }
            it("should satisfy laws with String values") { law.assertSatisfyingComonadLaw(list fmap { "v$it" }) }
        }

        context("as ComonadTrans") {
            val f = { w: K1<Base, Int> -> Env.narrow(w).let { "${it.env}:${it.value}" } }

            it("should extract the lowered head") { (env.extract(instance.lower(list)) == instance.extract(list)).shouldBeTrue() }

            it("should commute lower with extend") {
                val left = instance.lower(instance.extend({ w -> f(instance.lower(w)) }, list))
                cofreeTEq(left, env.extend(f, instance.lower(list))).shouldBeTrue()
            }
        }

        context("operations") {
            it("should unfold with coiterT and expose the tail with unwrap") { toList(list) shouldBe listOf(0, 1, 2, 3) }
            it("should lower to the base comonad of heads") { list.lower() shouldBe Env("ctx", 0) }
            it("should map every annotation") { toList(list fmap { it * 10 }) shouldBe listOf(0, 10, 20, 30) }

            it("should extend with the sub-structure at every node") {
                toList(list extend { w -> toList(CofreeT.narrow(w)).sum() }) shouldBe listOf(6, 6, 5, 3)
            }

            it("should describe infinite streams lazily") {
                val identity = object : IdentityComonad {}
                val naturals = CofreeT.coiterT(object : IdentityFunctor {}, identity, Identity(0)) { w -> Identity(Identity.narrow(w) fmap { it + 1 }) }
                val next = { c: CofreeT<Identity.T, Identity.T, Int> -> Identity.narrow(c.unwrap()).value }
                next(next(naturals extend { w -> CofreeT.narrow(w).extract() * 2 })).extract() shouldBe 4
            }
        }
    }
})

package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun Maybe<Any?>.toList(): List<Any?> = MaybeToListK(this).value

private fun <A> lower(v: K1<*, A>): List<A> {
    @Suppress("UNCHECKED_CAST")
    val day = v as Day<Maybe.T, ListK.T, A>
    return Maybe.narrow(day.left).toList().flatMap { b -> ListK.narrow(day.right).value.map { c -> day.combine(b, c) } }
}

// Day<Identity, Env<E>, A> is isomorphic to Env<E, A>; compare through that isomorphism.
private fun normalize(v: Any?): Any? {
    if (v !is Day<*, *, *>) return v
    val env = v.right as Env<*, *>
    return Env(env.env, normalize(v.combine((v.left as Identity<*>).value, env.value)))
}

private fun deepEq(a: Any?, b: Any?): Boolean = normalize(a) == normalize(b)

class DayTest : DescribeSpec({
    describe("Day") {
        val maybeListDay = Day(Maybe.Just(2), listKOf(10, 20)) { b: Int, c: Int -> b * c }
        val identityEnvDay = Day(Identity(3), Env("env", 4)) { b: Int, c: Int -> b + c }

        context("as Functor") {
            val law = FunctorLaw(Day.functor<Maybe.T, ListK.T>()) { a, b -> lower(a) == lower(b) }

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(maybeListDay, { it + 1 }, { it.toString() }) }
        }

        context("as Applicative") {
            val applicative = Day.applicative(object : MaybeApplicative {}, object : ListKApplicative {})
            val law = ApplicativeLaw(applicative) { a, b -> lower(a) == lower(b) }
            val u = Day(Maybe.Just(1), listKOf(2, 3)) { b: Int, c: Int -> { x: String -> x.length * b + c } }
            val v = Day(Maybe.Just("a"), listKOf("b", "cc")) { b: String, c: String -> { x: Int -> b + c + x } }

            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(maybeListDay, 5, u, v, { x: Int -> x.toString() }) }
            it("should be empty when the Maybe side is empty") { lower(applicative.ap(u, Day(Maybe.None<Int>(), listKOf(1)) { _: Int, c: Int -> c.toString() })) shouldBe emptyList() }
        }

        context("as Comonad") {
            val comonad = Day.comonad(object : IdentityComonad {}, object : EnvComonad<String> {})
            val law = ComonadLaw(comonad, ::deepEq)

            it("should satisfy laws") { law.assertSatisfyingComonadLaw(identityEnvDay) }
            it("should extract by combining both sides") { comonad.extract(identityEnvDay) shouldBe 7 }
        }

        context("operations") {
            it("should map the result") { lower(maybeListDay fmap { it + 1 }) shouldBe listOf(21, 41) }
            it("should pair values with day") { lower(Day.day(Maybe.Just(1), listKOf("a"))) shouldBe listOf(Pair(1, "a")) }
            it("should swap both sides") { ListK.narrow(maybeListDay.trans1(MaybeToListK).swapped().dap(object : ListKApplicative {})) shouldBe listKOf(20, 40) }

            it("should collapse a day of the same applicative with dap") {
                val day = Day(listKOf(1, 2), listKOf(10, 20)) { b: Int, c: Int -> b + c }
                ListK.narrow(day.dap(object : ListKApplicative {})) shouldBe listKOf(11, 21, 12, 22)
            }

            it("should fold with access to both sides") {
                val size = maybeListDay.fold(object : Day.Fold<Maybe.T, ListK.T, Int, Int> {
                    override fun <B, C> invoke(left: K1<Maybe.T, B>, right: K1<ListK.T, C>, combine: (B, C) -> Int): Int =
                        ListK.narrow(right).value.size
                })
                size shouldBe 2
            }
        }
    }
})

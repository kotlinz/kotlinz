package com.github.kotlinz.kotlinz.data.freeap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private class Read<A>(val key: String, val parse: (String) -> A): K1<Read.T, A> {
    class T
}

private fun <A> read(key: String, parse: (String) -> A): FreeAp<Read.T, A> = FreeAp.lift(Read(key, parse))

private fun config(values: Map<String, String>) = object : NaturalTransformation<Read.T, Identity.T> {
    override fun <A> invoke(fa: K1<Read.T, A>): Identity<A> = (fa as Read<A>).let { Identity(it.parse(values.getValue(it.key))) }
}

private val keys = object : NaturalTransformation<Read.T, K1<Const.T, StringMonoid>> {
    override fun <B> invoke(fa: K1<Read.T, B>): Const<StringMonoid, B> = Const(StringMonoid((fa as Read<B>).key + ";"))
}

private val count = object : NaturalTransformation<Read.T, K1<Const.T, SumMonoid>> {
    override fun <A> invoke(fa: K1<Read.T, A>): Const<SumMonoid, A> = Const(SumMonoid(1))
}

private fun lowerList(v: K1<*, *>): ListK<*> {
    @Suppress("UNCHECKED_CAST")
    return ListK.narrow((v as FreeAp<ListK.T, Any?>).retract(object : ListKApplicative {}))
}

class FreeApTest : DescribeSpec({
    describe("FreeAp") {
        val program = FreeAp.lift(listKOf(1, 2))
        val applicative = FreeAp.applicative<ListK.T>()

        context("as Functor") {
            val law = FunctorLaw(applicative) { a, b -> lowerList(a) == lowerList(b) }

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(program, { it + 1 }, { it.toString() }) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(applicative) { a, b -> lowerList(a) == lowerList(b) }
            val u = FreeAp.lift(listKOf({ s: String -> s + "!" }, { s: String -> s + "?" }))
            val v = FreeAp.lift(listKOf({ n: Int -> n.toString() }, { n: Int -> "#$n" }))

            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(program, 3, u, v, { n: Int -> n.toString() }) }
        }

        context("interpretation") {
            val user = read("name") { it } ap (read("age") { it.toInt() } fmap { age -> { name: String -> "$name:$age" } })

            it("should fold into Identity with a natural transformation") {
                Identity.narrow(user.foldMap(config(mapOf("name" to "alice", "age" to "30")), object : IdentityMonad {})).value shouldBe "alice:30"
            }

            it("should analyze the program statically") { user.analyze(StringMonoid::class.java, keys).value shouldBe "age;name;" }
            it("should count the instructions without running them") { user.analyze(SumMonoid::class.java, count).value shouldBe 2 }

            it("should keep effect order when retracting") {
                val pairs = FreeAp.lift(listKOf(1, 2)) ap (FreeAp.lift(listKOf("a", "b")) fmap { s -> { n: Int -> "$s$n" } })
                ListK.narrow(pairs.retract(object : ListKApplicative {})) shouldBe listKOf("a1", "a2", "b1", "b2")
            }

            it("should hoist into another functor") {
                val hoisted = user.hoist(config(mapOf("name" to "bob", "age" to "4")))
                Identity.narrow(hoisted.retract(object : IdentityMonad {})).value shouldBe "bob:4"
            }

            it("should lift pure values") { ListK.narrow(FreeAp.pure<ListK.T, Int>(1).retract(object : ListKApplicative {})) shouldBe listKOf(1) }
        }
    }
})

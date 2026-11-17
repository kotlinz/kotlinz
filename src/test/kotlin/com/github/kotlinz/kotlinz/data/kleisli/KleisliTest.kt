package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.ChoiceLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import com.github.kotlinz.kotlinz.law.StrongLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KleisliTest : DescribeSpec({
    val monad = object : IdentityMonad {}

    describe("Kleisli") {
        context("as Profunctor") {
            val law = ProfunctorLaw(object : KleisliProfunctor<Identity.T> { override val monad = monad }, ::sameKleisli)

            it("should satisfy laws") { law.assertSatisfyingProfunctorLaw(Kleisli(monad) { a: Int -> Identity(a * 10) }) }

            it("should map the input and the output with dimap") {
                val k = Kleisli(monad) { a: Int -> Identity(a * 10) }.dimap({ s: String -> s.length }, { b: Int -> "result: $b" })
                k.run("abc") shouldBe Identity("result: 30")
            }

            it("should map the input with lmap and the output with rmap") {
                val k = Kleisli(monad) { a: Int -> Identity(a + 1) }
                (k lmap { s: String -> s.length }).run("ab") shouldBe Identity(3)
                (k rmap { b: Int -> b.toString() }).run(1) shouldBe Identity("2")
            }
        }

        context("as Strong") {
            val law = StrongLaw(object : KleisliStrong<Identity.T> { override val monad = monad }, ::observe)

            it("should satisfy laws") { law.assertSatisfyingStrongLaw(Kleisli(monad) { a: Int -> Identity(a * 10) }) }

            it("should pass the other component through") {
                Kleisli(monad) { a: Int -> Identity(a + 1) }.first<String>().run(Pair(1, "x")) shouldBe Identity(Pair(2, "x"))
            }
        }

        context("as Choice") {
            val law = ChoiceLaw(object : KleisliChoice<Identity.T> { override val monad = monad }, ::observe)

            it("should satisfy laws") { law.assertSatisfyingChoiceLaw(Kleisli(monad) { a: Int -> Identity(a * 10) }) }

            it("should run only on the selected side") {
                val k = Kleisli(monad) { a: Int -> Identity(a + 1) }
                k.left<String>().run(Either.Left(1)) shouldBe Identity(Either.Left<Int, String>(2))
                k.left<String>().run(Either.Right("x")) shouldBe Identity(Either.Right<Int, String>("x"))
                k.right<String>().run(Either.Right(1)) shouldBe Identity(Either.Right<String, Int>(2))
            }
        }
    }
})

@Suppress("UNCHECKED_CAST")
private fun observe(v: K3<Kleisli.T, Identity.T, *, *>): (Any?) -> Any? = (v as Kleisli<Identity.T, Any?, Any?>).run

private fun sameKleisli(a: K3<Kleisli.T, Identity.T, Int, Int>, b: K3<Kleisli.T, Identity.T, Int, Int>): Boolean =
        (-10..10).all { Kleisli.narrow(a).run(it) == Kleisli.narrow(b).run(it) }

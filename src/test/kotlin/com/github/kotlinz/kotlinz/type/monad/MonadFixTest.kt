package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.eval.Eval
import com.github.kotlinz.kotlinz.data.eval.EvalMonadFix
import com.github.kotlinz.kotlinz.data.function.Function0
import com.github.kotlinz.kotlinz.data.function.Function0MonadFix
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadFix
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonadFix
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ReaderMonadFix
import com.github.kotlinz.kotlinz.law.LazyStream
import com.github.kotlinz.kotlinz.law.MonadFixLaw
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <T> checkLaws(monad: MonadFix<T>, eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) =
    MonadFixLaw(monad, eq).assertSatisfyingMonadFixLaw(
        { x -> LazyStream(1, x) },
        monad.pure(2), { x, y: Int -> monad.pure(LazyStream(y, x)) },
        { x -> monad.pure(LazyStream(1, x)) }, { s -> LazyStream(s.head + 1, s.tail) },
        { x, y -> monad.pure(LazyStream(1) { LazyStream(x().head + 1, y) }) })

class MonadFixTest : DescribeSpec({
    describe("MonadFix") {
        describe("Identity") {
            val monad = object : IdentityMonadFix {}

            it("should satisfy MonadFix laws") { checkLaws(monad) }

            it("should tie a cyclic structure") {
                monad.mfix { self -> Identity(LazyStream(1) { LazyStream(2, self) }) }.value.take(5) shouldBe listOf(1, 2, 1, 2, 1)
            }

            it("should fail when the recursive value is forced too early") {
                shouldThrow<IllegalStateException> { monad.mfix { x: () -> Int -> Identity(x() + 1) } }
            }
        }

        describe("Maybe") {
            val monad = object : MaybeMonadFix {}

            it("should satisfy MonadFix laws") { checkLaws(monad) }

            it("should tie the knot through Just and propagate None") {
                monad.mfix { self -> Maybe.Just(LazyStream(3, self)) } shouldBe Maybe.Just(Knot.fix { s: () -> LazyStream -> LazyStream(3, s) })
                monad.mfix { _: () -> Int -> Maybe.None() } shouldBe Maybe.None<Int>()
            }
        }

        describe("Reader") {
            val monad = object : ReaderMonadFix<Int> {}
            val eq = { a: K1<*, *>, b: K1<*, *> -> (a as Reader<Int, *>).run(7) == (b as Reader<Int, *>).run(7) }

            it("should satisfy MonadFix laws") { checkLaws(monad, eq) }

            it("should tie the knot per environment") {
                val counter = monad.mfix { self -> Reader { r: Int -> LazyStream(r, self) } }
                counter.run(4).take(3) shouldBe listOf(4, 4, 4)
                counter.run(9).take(2) shouldBe listOf(9, 9)
            }
        }

        describe("Function0") {
            val monad = object : Function0MonadFix {}
            val eq = { a: K1<*, *>, b: K1<*, *> -> (a as Function0<*>).run() == (b as Function0<*>).run() }

            it("should satisfy MonadFix laws") { checkLaws(monad, eq) }
        }

        describe("Eval") {
            val monad = object : EvalMonadFix {}
            val eq = { a: K1<*, *>, b: K1<*, *> -> (a as Eval<*>).value == (b as Eval<*>).value }

            it("should satisfy MonadFix laws") { checkLaws(monad, eq) }

            it("should defer tying the knot until the value is requested") {
                var calls = 0
                val eval = monad.mfix { self -> calls++; Eval.now(LazyStream(5, self)) }
                calls shouldBe 0
                eval.value.take(3) shouldBe listOf(5, 5, 5)
                calls shouldBe 1
            }
        }
    }
})

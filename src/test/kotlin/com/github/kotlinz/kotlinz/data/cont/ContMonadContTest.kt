package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.MonadContLaw
import com.github.kotlinz.kotlinz.type.monad.MonadCont
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <T> safeDiv(m: MonadCont<T>, a: Int, b: Int): K1<T, String> =
    m.callCC { exit: (String) -> K1<T, Int> ->
        val quotient = m.bind({ _: Unit -> if (b == 0) exit("division by zero") else m.pure(a / b) }, m.pure(Unit))
        m.fmap({ q: Int -> "result: $q" }, quotient)
    }

class ContMonadContTest : DescribeSpec({
    describe("ContMonadCont") {
        val monad = object : ContMonadCont<Any?> {}
        val eq = { a: K1<*, *>, b: K1<*, *> ->
            (a as Cont<Any?, Any?>).run { it } == (b as Cont<Any?, Any?>).run { it }
        }

        it("should satisfy MonadCont laws") {
            MonadContLaw(monad, eq).assertSatisfyingMonadContLaw(3, Cont.pure(4)) { x: Int -> Cont.pure(x * 100) }
        }

        it("should escape from polymorphic code") {
            Cont.narrow(safeDiv(monad, 10, 2)).run { it } shouldBe "result: 5"
            Cont.narrow(safeDiv(monad, 10, 0)).run { it } shouldBe "division by zero"
        }

        it("should resume the outer continuation after escaping") {
            val block = monad.callCC { exit: (Int) -> K1<K1<Cont.T, Any?>, Int> -> monad.bind({ x: Int -> Cont.pure<Any?, Int>(x + 1000) }, exit(1)) }
            (block fmap { it + 1 }).run { it } shouldBe 2
        }
    }
})

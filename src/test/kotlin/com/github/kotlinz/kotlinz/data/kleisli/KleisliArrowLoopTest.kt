package com.github.kotlinz.kotlinz.data.kleisli

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadFix
import com.github.kotlinz.kotlinz.law.LazyStream
import com.github.kotlinz.kotlinz.type.monad.Knot
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias IdK<A, B> = K3<Kleisli.T, Identity.T, A, B>

class KleisliArrowLoopTest : DescribeSpec({
    describe("KleisliArrowLoop") {
        val monad = object : IdentityMonadFix {}
        val arrow = object : KleisliArrowLoop<Identity.T> { override val monad = monad }
        fun <A, B> run(k: IdK<A, B>, a: A): B = Identity.narrow(Kleisli.narrow(k).run(a)).value

        val step = { p: Pair<Int, () -> LazyStream> -> Pair(LazyStream(p.first, p.second), LazyStream(p.first + 1, p.second)) }
        val f: IdK<Pair<Int, () -> LazyStream>, Pair<LazyStream, LazyStream>> = arrow.arr(step)

        it("should feed the second output back as the second input") {
            val ones = Kleisli.loop(monad, Kleisli(monad) { p: Pair<Int, () -> LazyStream> -> Identity(Pair(p.second, LazyStream(p.first, p.second))) })
            Identity.narrow(ones.run(1)).value().take(4) shouldBe listOf(1, 1, 1, 1)
        }

        it("should satisfy the extension law") {
            val expected = Knot.fix { p: () -> Pair<LazyStream, LazyStream> -> step(Pair(3, { p().second })) }.first
            run(arrow.loop(f), 3) shouldBe expected
            run(arrow.loop(f), 3).take(3) shouldBe listOf(3, 4, 4)
        }

        it("should satisfy left tightening") {
            val h: IdK<Int, Int> = arrow.arr { n: Int -> n * 10 }
            val left = arrow.loop(arrow.compose(f, arrow.first<Int, Int, () -> LazyStream>(h)))
            run(left, 2) shouldBe run(arrow.compose(arrow.loop(f), h), 2)
        }

        it("should satisfy right tightening") {
            val h: IdK<LazyStream, LazyStream> = arrow.arr { s: LazyStream -> LazyStream(s.head * 2, s.tail) }
            val left = arrow.loop(arrow.compose(arrow.first<LazyStream, LazyStream, LazyStream>(h), f))
            run(left, 2) shouldBe run(arrow.compose(h, arrow.loop(f)), 2)
        }
    }
})

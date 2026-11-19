package com.github.kotlinz.kotlinz.data.listt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListTTest : DescribeSpec({
    describe("ListT") {
        val identity = object : IdentityMonad {}
        val io = object : IOMonad {}
        fun <A> ids(vararg xs: A) = ListT.fromList(identity, xs.toList())
        val observe = { v: K1<K1<ListT.T, Identity.T>, *> -> Identity.narrow(ListT.narrow(v).toList()).value }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(object : ListTMonad<Identity.T> { override val monad = identity }, observe)
            val f = { x: Int -> ids(x, x + 1) }
            val g = { x: Int -> if (x % 2 == 0) ids(x * 10) else ids() }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, ids(1, 2, 3), f, g) }
            it("should satisfy laws with the empty stream") { law.assertSatisfyingMonadLaw(1, ids(), f, g) }
        }

        context("as Monad over IO with an effect log") {
            val log = mutableListOf<String>()
            val law = ObservedMonadLaw(object : ListTMonad<IO.T> { override val monad = io }) {
                IO.narrow(ListT.narrow(it).toList()).unsafeRun() to log.toList().also { log.clear() }
            }
            fun tick(x: Int): ListT<IO.T, Int> = ListT.lift(io, IO { log += "tick $x"; x })
            val f = { x: Int -> tick(x) orElse tick(x + 1) }
            val g = { x: Int -> tick(x * 10) }

            it("should satisfy laws including effect order") {
                law.assertSatisfyingMonadLaw(1, tick(1) orElse tick(2), f, g)
            }
        }

        context("as MonadPlus") {
            val instance = object : ListTMonadPlus<Identity.T> { override val monad = identity }
            val (a, b, c) = Triple(ids(1, 2), ids(3), ids<Int>())

            it("should satisfy Alternative laws") {
                listOf(a, b, c).forEach { m ->
                    observe(instance.orElse(instance.empty(), m)) shouldBe observe(m)
                    observe(instance.orElse(m, instance.empty())) shouldBe observe(m)
                }
                observe(instance.orElse(instance.orElse(a, b), c)) shouldBe observe(instance.orElse(a, instance.orElse(b, c)))
            }

            it("should satisfy zero laws") {
                observe(instance.bind({ x: Int -> ids(x) }, instance.mzero<Int>())) shouldBe listOf()
                observe(instance.bind({ _: Int -> instance.mzero<Int>() }, a)) shouldBe listOf()
            }
        }

        context("streaming") {
            fun nats(n: Int): ListT<Identity.T, Int> = ListT.cons(identity, n, ListT.defer(identity) { nats(n + 1) })

            it("should take from an infinite stream") {
                observe(nats(0).take(3)) shouldBe listOf(0, 1, 2)
                observe((nats(0) bind { if (it % 2 == 0) ids(it) else ids() }).take(3)) shouldBe listOf(0, 2, 4)
            }

            it("should interleave effects with elements and stop early") {
                val log = mutableListOf<String>()
                val xs = ListT.fromList(io, listOf(1, 2, 3)) bind { x -> ListT.liftIO(object : IOMonadIO {}, IO { log += "run $x"; x * 10 }) }
                log shouldBe listOf()
                IO.narrow(xs.take(2).toList()).unsafeRun() shouldBe listOf(10, 20)
                log shouldBe listOf("run 1", "run 2")
            }

            it("should apply functions to every element") {
                observe(ids(1, 2) ap ids({ x: Int -> x + 1 }, { x: Int -> x * 10 })) shouldBe listOf(2, 3, 10, 20)
            }
        }

        context("fx") {
            it("should enumerate combinations with replay") {
                val pairs = ListT.fx(identity) { Pair(ids(1, 2).bind(), ids("a", "b").bind()) }
                observe(pairs) shouldBe listOf(1 to "a", 1 to "b", 2 to "a", 2 to "b")
            }
        }
    }
})

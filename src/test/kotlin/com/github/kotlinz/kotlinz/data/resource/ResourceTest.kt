package com.github.kotlinz.kotlinz.data.resource

import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ResourceTest : DescribeSpec({
    describe("Resource") {
        val log = mutableListOf<String>()
        fun tracked(name: String): Resource<String> =
            Resource.make(IO { log += "acquire $name"; name }) { IO { log += "release $it" } }
        fun failing(name: String): Resource<String> = Resource.make(IO { throw IllegalStateException(name) }) { IO {} }

        beforeEach { log.clear() }

        context("as Monad") {
            val law = ObservedMonadLaw(object : ResourceMonad {}) { r ->
                Resource.narrow(r).use { IO.pure(it) }.unsafeRun() to log.toList().also { log.clear() }
            }
            val f = { x: String -> tracked(x + "f") }
            val g = { x: String -> tracked(x + "g") fmap { it.length.toString() } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw("a", tracked("a"), f, g) }
            it("should satisfy laws with pure") { law.assertSatisfyingMonadLaw("a", Resource.pure("a"), f, g) }
        }

        context("use") {
            it("should acquire, use and release") {
                tracked("a").use { a -> IO { log += "use $a"; a.length } }.unsafeRun() shouldBe 1
                log shouldBe listOf("acquire a", "use a", "release a")
            }

            it("should release even when use fails") {
                shouldThrow<IllegalStateException> { tracked("a").use<Int> { IO { throw IllegalStateException() } }.unsafeRun() }
                log shouldBe listOf("acquire a", "release a")
            }

            it("should attach a failing release as suppressed") {
                val r = Resource.make(IO.pure(1)) { IO { throw IllegalArgumentException("release") } }
                val e = shouldThrow<IllegalStateException> { r.use<Int> { IO { throw IllegalStateException("use") } }.unsafeRun() }
                e.suppressed.map { it.message } shouldBe listOf("release")
            }

            it("should be lazy and rerunnable") {
                val program = tracked("a").use { IO.pure(it) }
                log shouldBe listOf()
                program.unsafeRun()
                program.unsafeRun()
                log.size shouldBe 4
            }
        }

        context("composition") {
            it("should release in reverse order of acquisition") {
                val both = tracked("a") bind { a -> tracked("b") fmap { b -> a + b } }
                both.use { IO { log += "use $it" } }.unsafeRun()
                log shouldBe listOf("acquire a", "acquire b", "use ab", "release b", "release a")
            }

            it("should release acquired resources when a later acquisition fails") {
                val program = tracked("a") bind { tracked("b") } bind { failing("c") }
                shouldThrow<IllegalStateException> { program.use { IO.pure(it) }.unsafeRun() }.message shouldBe "c"
                log shouldBe listOf("acquire a", "acquire b", "release b", "release a")
            }

            it("should run every release even when one fails") {
                val broken = Resource.make(IO.pure("x")) { IO { log += "release x"; throw IllegalStateException("x") } }
                val program = tracked("a") bind { broken } bind { tracked("b") }
                shouldThrow<IllegalStateException> { program.use { IO.pure(it) }.unsafeRun() }
                log shouldBe listOf("acquire a", "acquire b", "release b", "release x", "release a")
            }

            it("should acquire the function first with ap") {
                val r = tracked("b") ap (tracked("a") fmap { a -> { b: String -> a + b } })
                r.use { IO.pure(it) }.unsafeRun() shouldBe "ab"
                log shouldBe listOf("acquire a", "acquire b", "release b", "release a")
            }

            it("should not release lifted IO values") {
                Resource.liftIO(IO { 1 }).use { IO.pure(it + 1) }.unsafeRun() shouldBe 2
            }
        }

        context("fx") {
            it("should compose resources and release them in reverse order") {
                val program = Resource.fx {
                    val a = tracked("a").bind()
                    val b = tracked("b").bind()
                    a + b
                }
                program.use { IO.pure(it) }.unsafeRun() shouldBe "ab"
                program.use { IO.pure(it) }.unsafeRun() shouldBe "ab"
                log shouldBe listOf("acquire a", "acquire b", "release b", "release a").let { it + it }
            }
        }
    }
})

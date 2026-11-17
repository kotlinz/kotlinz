package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WriterMonadWriterTest : DescribeSpec({
    describe("Writer") {
        val type = StringMonoid::class.java
        val monad = object : WriterMonadWriter<StringMonoid> { override val type = type }

        context("as MonadWriter") {
            val law = MonadWriterLaw(monad, StringMonoid(""), { a, b -> StringMonoid(a.value + b.value) }) { (it as Writer<*, *>).run }
            it("should satisfy laws") { law.assertSatisfyingMonadWriterLaw(1, StringMonoid("a"), StringMonoid("b")) }
        }

        context("with primitives") {
            it("should accumulate output with tell") {
                val program = Writer.tell(type, StringMonoid("a")) bind { Writer.tell(type, StringMonoid("b")) }
                program.run shouldBe Pair(Unit, StringMonoid("ab"))
            }

            it("should expose output with listen") {
                Writer.writer(type, Pair(1, StringMonoid("log"))).listen().run shouldBe Pair(Pair(1, StringMonoid("log")), StringMonoid("log"))
            }

            it("should modify output with censor") {
                Writer.writer(type, Pair(1, StringMonoid("log"))).censor { StringMonoid(it.value.uppercase()) }.run shouldBe Pair(1, StringMonoid("LOG"))
            }

            it("should project output with listens") {
                val listened = Writer.narrow(monad.listens({ it.value.length }, Writer.tell(type, StringMonoid("abc"))))
                listened.run shouldBe Pair(Pair(Unit, 3), StringMonoid("abc"))
            }
        }
    }
})

package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReaderMonadReaderTest : DescribeSpec({
    describe("Reader") {
        val monad = object : ReaderMonadReader<Int> {}

        context("as MonadReader") {
            val law = MonadReaderLaw(monad) { (it as Reader<Int, *>).run(3) }
            it("should satisfy laws") {
                law.assertSatisfyingMonadReaderLaw(Reader<Int, Int> { it * 10 }, { it + 1 }, { it * 2 })
            }
        }

        context("with primitives") {
            it("should ask the environment") {
                Reader.ask<Int>().run(42) shouldBe 42
            }

            it("should project the environment with reader") {
                Reader.reader<String, Int> { it.length }.run("kotlinz") shouldBe 7
            }

            it("should modify the environment with local") {
                monad.local({ it * 2 }, Reader.ask<Int>()).run(21) shouldBe 42
            }
        }
    }
})

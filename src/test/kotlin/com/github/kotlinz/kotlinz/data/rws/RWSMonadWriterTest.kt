package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RWSMonadWriterTest : DescribeSpec({
    describe("RWS") {
        val type = StringMonoid::class.java
        val monad = object : RWSMonadWriter<Int, StringMonoid, Int> { override val type = type }

        context("as MonadWriter") {
            val combine = { a: StringMonoid, b: StringMonoid -> StringMonoid(a.value + b.value) }
            val law = MonadWriterLaw(monad, StringMonoid(""), combine) { (it as RWS<Int, StringMonoid, Int, *>).run(3, 5) }
            it("should satisfy laws") { law.assertSatisfyingMonadWriterLaw(1, StringMonoid("a"), StringMonoid("b")) }
        }

        context("with primitives") {
            it("should write a value and output") {
                RWS.writer<Int, StringMonoid, Int, Int>(type, Pair(1, StringMonoid("w"))).run(0, 2) shouldBe Triple(1, 2, StringMonoid("w"))
            }

            it("should keep state while listening") {
                val m = RWS<Int, StringMonoid, Int, Int>(type) { r, s -> Triple(r, s + 1, StringMonoid("m")) }
                monad.listen(m).run(3, 5) shouldBe Triple(Pair(3, StringMonoid("m")), 6, StringMonoid("m"))
            }

            it("should modify output with censor") {
                val censored = RWS.narrow(monad.censor({ StringMonoid(it.value + "!") }, monad.tell(StringMonoid("w"))))
                censored.exec(0, 0) shouldBe Pair(0, StringMonoid("w!"))
            }
        }
    }
})

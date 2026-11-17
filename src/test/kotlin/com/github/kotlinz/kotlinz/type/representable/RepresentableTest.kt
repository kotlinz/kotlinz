package com.github.kotlinz.kotlinz.type.representable

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityRepresentable
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ReaderRepresentable
import com.github.kotlinz.kotlinz.data.twin.Twin
import com.github.kotlinz.kotlinz.data.twin.TwinRepresentable
import com.github.kotlinz.kotlinz.law.RepresentableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val readerSamples = listOf(-1, 0, 42)

private val readerEq = { a: K1<K1<Reader.T, Int>, *>, b: K1<K1<Reader.T, Int>, *> ->
    readerSamples.all { Reader.narrow(a).run(it) == Reader.narrow(b).run(it) }
}

class RepresentableTest : DescribeSpec({
    describe("Representable") {
        context("Identity") {
            val law = RepresentableLaw(object : IdentityRepresentable {}, listOf(Unit))

            it("should satisfy laws") { law.assertSatisfyingRepresentableLaw({ _: Unit -> 1 }, Identity(2)) }
        }

        context("Reader") {
            val law = RepresentableLaw(object : ReaderRepresentable<Int> {}, readerSamples, readerEq)

            it("should satisfy laws") { law.assertSatisfyingRepresentableLaw({ r: Int -> r * 2 }, Reader { r: Int -> "$r" }) }
        }

        context("Twin") {
            val law = RepresentableLaw(object : TwinRepresentable {}, listOf(false, true))

            it("should satisfy laws") { law.assertSatisfyingRepresentableLaw({ b: Boolean -> if (b) "yes" else "no" }, Twin(1, 2)) }
            it("should tabulate from a function") { Twin.tabulate { if (it) 1 else 0 } shouldBe Twin(0, 1) }
            it("should index by Boolean") { Twin("a", "b").index(true) shouldBe "b" }
            it("should map both components") { Twin(1, 2) fmap { it * 10 } shouldBe Twin(10, 20) }
            it("should swap components") { Twin(1, 2).swap() shouldBe Twin(2, 1) }
        }
    }
})

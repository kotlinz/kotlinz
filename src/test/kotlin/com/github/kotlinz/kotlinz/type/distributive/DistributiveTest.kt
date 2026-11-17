package com.github.kotlinz.kotlinz.type.distributive

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityDistributive
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ReaderDistributive
import com.github.kotlinz.kotlinz.law.DistributiveLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val samples = listOf(-1, 0, 5)

@Suppress("UNCHECKED_CAST")
private fun readerEq(a: Any?, b: Any?): Boolean = when {
    a is Reader<*, *> && b is Reader<*, *> ->
        samples.all { readerEq((a as Reader<Int, Any?>).run(it), (b as Reader<Int, Any?>).run(it)) }
    a is Identity<*> && b is Identity<*> -> readerEq(a.value, b.value)
    else -> a == b
}

class DistributiveTest : DescribeSpec({
    describe("Distributive") {
        val listFunctor = object : ListKFunctor {}

        context("Identity") {
            val distributive = object : IdentityDistributive {}
            val law = DistributiveLaw(distributive)

            it("should satisfy laws") { law.assertSatisfyingDistributiveLaw(Identity(1), Identity(Identity(2)), { a: Int -> Identity("$a") }) }

            it("should distribute over a list") {
                distributive.distribute(listFunctor, listKOf(Identity(1), Identity(2))) shouldBe Identity(listKOf(1, 2))
            }

            it("should cotraverse a list") {
                val sum = { l: K1<ListK.T, Int> -> ListK.narrow(l).value.sum() }
                distributive.cotraverse(listFunctor, sum, listKOf(Identity(1), Identity(2))) shouldBe Identity(3)
            }
        }

        context("Reader") {
            val distributive = object : ReaderDistributive<Int> {}
            val law = DistributiveLaw(distributive, ::readerEq)

            it("should satisfy laws") {
                law.assertSatisfyingDistributiveLaw(
                        Reader { r: Int -> r + 1 },
                        Reader { r: Int -> Reader { s: Int -> r * 10 + s } },
                        { a: Int -> Reader { r: Int -> "$a:$r" } }
                )
            }

            it("should distribute over a list") {
                val readers = listKOf<K1<K1<Reader.T, Int>, Int>>(Reader { r: Int -> r + 1 }, Reader { r: Int -> r * 2 })
                Reader.narrow(distributive.distribute(listFunctor, readers)).run(3) shouldBe listKOf(4, 6)
            }

            it("should collect over a list") {
                val collected = distributive.collect(listFunctor, { a: Int -> Reader { r: Int -> a + r } }, listKOf(1, 2))
                Reader.narrow(collected).run(10) shouldBe listKOf(11, 12)
            }
        }
    }
})

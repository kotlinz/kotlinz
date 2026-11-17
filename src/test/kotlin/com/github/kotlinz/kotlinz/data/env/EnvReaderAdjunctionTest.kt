package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.store.Store
import com.github.kotlinz.kotlinz.data.store.StoreComonad
import com.github.kotlinz.kotlinz.law.AdjunctionLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

private typealias E<A> = K1<K1<Env.T, Int>, A>
private typealias R<A> = K1<K1<Reader.T, Int>, A>

private val samples = listOf(-2, 0, 3)

@Suppress("UNCHECKED_CAST")
private fun observeEq(a: Any?, b: Any?): Boolean = when {
    a is Reader<*, *> && b is Reader<*, *> ->
        samples.all { observeEq((a as Reader<Int, Any?>).run(it), (b as Reader<Int, Any?>).run(it)) }
    a is Env<*, *> && b is Env<*, *> -> a.env == b.env && observeEq(a.value, b.value)
    a is Store<*, *> && b is Store<*, *> -> observeEq(a.toEnvReader(), b.toEnvReader())
    else -> a == b
}

class EnvReaderAdjunctionTest : DescribeSpec({
    describe("EnvReaderAdjunction") {
        val adjunction = object : EnvReaderAdjunction<Int> {}

        context("as Adjunction") {
            val law = AdjunctionLaw(adjunction, ::observeEq)

            it("should satisfy laws") {
                law.assertSatisfyingAdjunctionLaw(
                        "a",
                        Env(1, "fa"),
                        Reader { s: Int -> "ga$s" },
                        { a: String -> Reader { s: Int -> a.length + s } },
                        { fa: K1<K1<Env.T, Int>, String> -> Env.narrow(fa).let { it.value.length * it.env } }
                )
            }
        }

        context("State") {
            val state = State { s: Int -> Pair(s * 2, s + 1) }
            val f = { a: Int -> State { s: Int -> Pair("$a/$s", s * 10) } }

            it("should be isomorphic to Reader of Env") {
                samples.all { state.toReaderEnv().toState().run(it) == state.run(it) }.shouldBeTrue()
            }

            it("should bind through counit of the adjunction") {
                val composed = state.toReaderEnv().fmap<E<R<E<String>>>> { e -> Env.narrow(e).fmap<R<E<String>>> { a -> f(a).toReaderEnv() } }
                val joined = Reader.narrow(adjunction.joinComposed(composed)).toState()
                samples.all { joined.run(it) == (state bind f).run(it) }.shouldBeTrue()
            }
        }

        context("Store") {
            val store = Store({ s: Int -> s * 3 }, 4)

            it("should be isomorphic to Env of Reader") {
                observeEq(store.toEnvReader().toStore(), store).shouldBeTrue()
            }

            it("should duplicate through unit of the adjunction") {
                val duplicated = Env.narrow(adjunction.duplicateComposed(store.toEnvReader()))
                        .fmap<R<Store<Int, Int>>> { r -> Reader.narrow(r).fmap { e -> Env.narrow(e).toStore() } }
                        .toStore()
                observeEq(duplicated, object : StoreComonad<Int> {}.duplicate(store)).shouldBeTrue()
            }

            it("should extract through counit of the adjunction") {
                adjunction.counit(store.toEnvReader()) shouldBe store.extract()
            }
        }
    }
})

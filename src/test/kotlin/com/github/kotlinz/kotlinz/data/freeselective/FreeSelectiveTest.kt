package com.github.kotlinz.kotlinz.data.freeselective

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeSelective
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.SelectiveLaw
import com.github.kotlinz.kotlinz.type.monad.MonadSelective
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private class Read<A>(val key: String, val parse: (String) -> A): K1<Read.T, A> {
    class T
}

private fun <A> read(key: String, parse: (String) -> A): FreeSelective<Read.T, A> = FreeSelective.lift(Read(key, parse))

private fun config(values: Map<String, String>) = object : NaturalTransformation<Read.T, Maybe.T> {
    override fun <A> invoke(fa: K1<Read.T, A>): Maybe<A> =
        (fa as Read<A>).let { r -> values[r.key]?.let { Maybe.Just(r.parse(it)) } ?: Maybe.None() }
}

private val keys = object : NaturalTransformation<Read.T, K1<Const.T, StringMonoid>> {
    override fun <B> invoke(fa: K1<Read.T, B>): Const<StringMonoid, B> = Const(StringMonoid((fa as Read<B>).key + ";"))
}

private typealias Trace = K1<State.T, List<String>>

private fun trace(values: Map<String, String>) = object : NaturalTransformation<Read.T, Trace> {
    override fun <A> invoke(fa: K1<Read.T, A>): State<List<String>, A> =
        (fa as Read<A>).let { r -> State { log: List<String> -> Pair(r.parse(values.getValue(r.key)), log + r.key) } }
}

private fun lowerMaybe(v: K1<*, *>): Maybe<*> {
    @Suppress("UNCHECKED_CAST")
    return Maybe.narrow((v as FreeSelective<Maybe.T, Any?>).retract(object : MaybeSelective {}))
}

private fun <L, R> left(v: L): Either<L, R> = Either.Left(v)
private fun <L, R> right(v: R): Either<L, R> = Either.Right(v)

class FreeSelectiveTest : DescribeSpec({
    describe("FreeSelective") {
        val selective = FreeSelective.selective<Maybe.T>()
        fun <A> lift(m: Maybe<A>) = FreeSelective.lift(m)

        context("as Functor") {
            it("should satisfy laws") {
                FunctorLaw(selective) { a, b -> lowerMaybe(a) == lowerMaybe(b) }
                    .assertSatisfyingFunctorLaw(lift(Maybe.Just(1)), { it + 1 }, { it.toString() })
            }
        }

        context("as Applicative") {
            it("should satisfy laws") {
                ApplicativeLaw(selective) { a, b -> lowerMaybe(a) == lowerMaybe(b) }.assertSatisfyingApplicativeLaw(
                    lift(Maybe.Just(1)), 3, lift(Maybe.Just { s: String -> "$s!" }), lift(Maybe.Just { n: Int -> "#$n" }), { n: Int -> "$n" })
            }
        }

        context("as Selective") {
            val add = { a: Int -> { b: Int -> a + b } }
            listOf(left<Int, Int>(1), right(2)).forEach { e ->
                it("should satisfy laws with $e") {
                    SelectiveLaw(selective) { a, b -> lowerMaybe(a) == lowerMaybe(b) }.assertSatisfyingSelectiveLaw(
                        lift(Maybe.Just(left<Int, Int>(2))), lift(Maybe.Just(left<Int, (Int) -> Int>(3))), lift(Maybe.Just(add)),
                        lift(Maybe.Just(e)), e, lift(Maybe.Just { n: Int -> n + 1 }), lift(Maybe.None()))
                }
            }
        }

        context("programs") {
            val s = FreeSelective.selective<Read.T>()
            val program = FreeSelective.narrow(s.ifS(read("verbose") { it == "yes" }, read("log") { it }, FreeSelective.pure("quiet")))

            it("should skip the branch not taken when interpreted") {
                Maybe.narrow(program.foldMap(config(mapOf("verbose" to "no")), object : MaybeSelective {})) shouldBe Maybe.Just("quiet")
                Maybe.narrow(program.foldMap(config(mapOf("verbose" to "yes", "log" to "on")), object : MaybeSelective {})) shouldBe Maybe.Just("on")
                Maybe.narrow(program.foldMap(config(mapOf("verbose" to "yes")), object : MaybeSelective {})) shouldBe Maybe.None<String>()
            }

            it("should run only the selected instructions in a monad") {
                val monad = object : MonadSelective<Trace>, StateMonad<List<String>> {}
                State.narrow(program.foldMap(trace(mapOf("verbose" to "no")), monad)).run(listOf()) shouldBe Pair("quiet", listOf("verbose"))
                State.narrow(program.foldMap(trace(mapOf("verbose" to "yes", "log" to "on")), monad)).run(listOf()) shouldBe
                    Pair("on", listOf("verbose", "log"))
            }

            it("should list every possible and every necessary instruction statically") {
                program.effects().map { (it as Read<*>).key } shouldBe listOf("verbose", "log")
                program.necessaryEffects().map { (it as Read<*>).key } shouldBe listOf("verbose")
                program.analyze(StringMonoid::class.java, keys).value shouldBe "verbose;log;"
            }

            it("should hoist into another functor") {
                val hoisted = program.hoist(config(mapOf("verbose" to "yes", "log" to "on")))
                Maybe.narrow(hoisted.retract(object : MaybeSelective {})) shouldBe Maybe.Just("on")
            }

            it("should treat lifted instructions as necessary") {
                read("a") { it }.necessaryEffects().size shouldBe 1
                FreeSelective.pure<Read.T, Int>(1).effects() shouldBe listOf()
            }
        }
    }
})

package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private data class Say<A>(val msg: String, val next: A) : K1<Say.T, A> {
    class T

    companion object {
        fun <A> narrow(v: K1<T, A>): Say<A> = v as Say<A>
    }
}

private object SayFunctor : Functor<Say.T> {
    override fun <A, B> fmap(f: (A) -> B, v: K1<Say.T, A>): Say<B> = Say.narrow(v).let { Say(it.msg, f(it.next)) }
}

private typealias Log = K1<State.T, List<String>>

private val state = object : StateMonad<List<String>> {}

private fun say(msg: String): FreeT<Say.T, Log, Unit> = FreeT.liftF(SayFunctor, state, Say(msg, Unit))

private fun <A> log(msg: String, v: A): FreeT<Say.T, Log, A> =
    FreeT.lift(SayFunctor, state, State.modify<List<String>> { it + "lift:$msg" } fmap { v })

private fun <A> runLog(v: K1<*, A>): Pair<A, List<String>> {
    val program = v as FreeT<Say.T, Log, A>
    val result = program.iterT { layer -> Say.narrow(layer).let { s -> State.modify<List<String>> { it + s.msg } bind { State.narrow(s.next) } } }
    return State.narrow(result).run(emptyList())
}

class FreeTTest : DescribeSpec({
    describe("FreeT") {
        val instance = FreeTMonad.of(SayFunctor, state)
        val m = say("a") bind { log("b", 1) }
        val f = { x: Int -> say("f$x") fmap { x + 1 } }
        val g = { x: Int -> log("g$x", x * 2) bind { y -> say("g$y") fmap { y } } }

        context("as Monad") {
            val law = ObservedMonadLaw(instance) { runLog(it) }
            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, m, f, g) }
        }

        context("as Functor") {
            val law = FunctorLaw(instance) { a, b -> runLog(a) == runLog(b) }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(m, { it + 1 }, { it.toString() }) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(instance) { a, b -> runLog(a) == runLog(b) }
            val u = say("u") fmap { { x: Int -> x * 3 } }
            val v = log("v", { x: Int -> x + 2 })
            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(m, 2, u, v) { it - 1 } }
        }

        context("with primitives") {
            it("should interleave instructions and inner effects with iterT") {
                runLog(m) shouldBe Pair(1, listOf("a", "lift:b"))
            }

            it("should interpret instructions with foldMap") {
                val nt = object : NaturalTransformation<Say.T, Log> {
                    override fun <A> invoke(fa: K1<Say.T, A>): State<List<String>, A> =
                        Say.narrow(fa).let { s -> State.modify<List<String>> { it + "nt:${s.msg}" } fmap { s.next } }
                }
                State.narrow(m.foldMap(nt)).run(emptyList()) shouldBe Pair(1, listOf("nt:a", "lift:b"))
            }

            it("should change the instruction functor with transFreeT") {
                val shout = object : NaturalTransformation<Say.T, Say.T> {
                    override fun <A> invoke(fa: K1<Say.T, A>): Say<A> = Say.narrow(fa).let { Say(it.msg.uppercase(), it.next) }
                }
                runLog(m.transFreeT(SayFunctor, shout)) shouldBe Pair(1, listOf("A", "lift:b"))
            }

            it("should change the inner monad with hoistFreeT") {
                val identity = object : IdentityMonad {}
                val maybe = object : MaybeMonad {}
                val toMaybe = object : NaturalTransformation<Identity.T, Maybe.T> {
                    override fun <A> invoke(fa: K1<Identity.T, A>): Maybe<A> = Maybe.Just(Identity.narrow(fa).value)
                }
                val program = FreeT.liftF(SayFunctor, identity, Say("x", 1)) bind { FreeT.lift(SayFunctor, identity, Identity(it + 1)) }
                val hoisted = program.hoistFreeT(maybe, toMaybe)
                hoisted.iterT { layer -> Say.narrow(layer).next } shouldBe Maybe.Just(2)
            }
        }

        context("fx") {
            it("should bind instructions and lifted effects") {
                val program = FreeT.fx(SayFunctor, state) {
                    say("hello").bind()
                    val x = log("x", 20).bind()
                    say("x=$x").bind()
                    x + 1
                }
                runLog(program) shouldBe Pair(21, listOf("hello", "lift:x", "x=20"))
            }
        }
    }
})

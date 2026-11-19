package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadRec
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.data.state.StateMonadRec
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private sealed class Counter<A>: K1<Counter.T, A> {
    class T
    class Get: Counter<Int>()
    class Add(val n: Int): Counter<Unit>()
}

private fun get(): Freer<Counter.T, Int> = Freer.singleton(Counter.Get())
private fun add(n: Int): Freer<Counter.T, Unit> = Freer.singleton(Counter.Add(n))

@Suppress("UNCHECKED_CAST")
private object CounterToState : NaturalTransformation<Counter.T, K1<State.T, Int>> {
    override fun <A> invoke(fa: K1<Counter.T, A>): State<Int, A> = when (val op = fa as Counter<A>) {
        is Counter.Get -> State { s -> Pair(s as A, s) }
        is Counter.Add -> State { s -> Pair(Unit as A, s + op.n) }
    }
}

private class Flip : K1<Flip.T, Boolean> { class T }

@Suppress("UNCHECKED_CAST")
private object FlipToListK : NaturalTransformation<Flip.T, ListK.T> {
    override fun <A> invoke(fa: K1<Flip.T, A>): ListK<A> = listKOf(true as A, false as A)
}

private fun <A> runCounter(freer: K1<*, A>, s: Int = 1): Pair<A, Int> =
    State.narrow((freer as Freer<Counter.T, A>).interpret(CounterToState, object : StateMonadRec<Int> {})).run(s)

@Suppress("UNCHECKED_CAST")
private tailrec fun <A> runPure(freer: Freer<Counter.T, A>, s: Int, log: List<String>): Pair<A, List<String>> =
    when (val view = freer.view()) {
        is FreerView.Return -> Pair(view.value, log)
        is FreerView.Bind<Counter.T, *, A> -> {
            val bind = view as FreerView.Bind<Counter.T, Any?, A>
            when (val op = bind.instruction as Counter<*>) {
                is Counter.Get -> runPure(bind.continuation(s), s, log + "get")
                is Counter.Add -> runPure(bind.continuation(Unit), s + op.n, log + "add${op.n}")
            }
        }
    }

@Suppress("UNCHECKED_CAST")
private tailrec fun countSteps(freer: Freer<Counter.T, Unit>, steps: Int): Int = when (val view = freer.view()) {
    is FreerView.Return -> steps
    is FreerView.Bind<Counter.T, *, Unit> -> countSteps((view as FreerView.Bind<Counter.T, Any?, Unit>).continuation(Unit), steps + 1)
}

private fun countDown(n: Int): Freer<Identity.T, Int> =
    if (n == 0) Freer.pure(0) else Freer.liftF<Identity.T, Int>(Identity(n - 1)) bind { countDown(it) }

class FreerTest : DescribeSpec({
    describe("Freer") {
        val monad = object : FreerMonad<Counter.T> {}
        val m = add(2) bind { get() }
        val f = { x: Int -> add(x) bind { get() fmap { it * 10 } } }
        val g = { x: Int -> get() fmap { it + x } }

        context("as Monad") {
            val law = ObservedMonadLaw(monad) { runCounter(it) }
            it("should satisfy laws") { law.assertSatisfyingMonadLaw(3, m, f, g) }
        }

        context("as Functor") {
            val law = FunctorLaw(monad) { a, b -> runCounter(a) == runCounter(b) }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(m, { it + 1 }, { it.toString() }) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(monad) { a, b -> runCounter(a) == runCounter(b) }
            val u = get() fmap { n -> { x: Int -> x * n } }
            val v = add(3) fmap { { x: Int -> x + 1 } }
            it("should satisfy laws") { law.assertSatisfyingApplicativeLaw(m, 2, u, v) { it - 1 } }
        }

        context("interpretation") {
            it("should interpret into State") { runCounter(m) shouldBe Pair(3, 3) }

            it("should interpret into a monad without MonadRec") {
                State.narrow(m.foldMap(CounterToState, object : StateMonad<Int> {})).run(1) shouldBe Pair(3, 3)
            }

            it("should support custom interpreters via view") {
                runPure(m bind { x -> add(x) }, 1, emptyList()) shouldBe Pair(Unit, listOf("add2", "get", "add3"))
            }

            it("should support fx") {
                val program = Freer.fx<Counter.T, String> {
                    add(5).bind()
                    val x = get().bind()
                    add(x).bind()
                    "x=$x"
                }
                runCounter(program, 0) shouldBe Pair("x=5", 10)
            }

            it("should support fx with multi-shot interpreters") {
                val program = Freer.fx<Flip.T, Pair<Boolean, Boolean>> {
                    Pair(Freer.liftF<Flip.T, Boolean>(Flip()).bind(), Freer.liftF<Flip.T, Boolean>(Flip()).bind())
                }
                ListK.narrow(program.foldMap(FlipToListK, object : ListKMonad {})).value.size shouldBe 4
            }
        }

        context("stack safety") {
            val n = 100_000

            it("should interpret left-nested binds") {
                val program = (1..n).fold(Freer.pure<Counter.T, Unit>(Unit)) { acc, _ -> acc bind { add(1) } }
                runCounter(program, 0).second shouldBe n
            }

            it("should step through left-nested binds via view") {
                val program = (1..n).fold(Freer.pure<Counter.T, Unit>(Unit)) { acc, _ -> acc bind { add(1) } }
                countSteps(program, 0) shouldBe n
            }

            it("should interpret right-nested binds into Identity") {
                val identity = NaturalTransformation.identity<Identity.T>()
                Identity.narrow(countDown(n).foldMap(identity, object : IdentityMonadRec {})).value shouldBe 0
            }
        }
    }
})

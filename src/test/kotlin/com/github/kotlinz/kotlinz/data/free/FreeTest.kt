package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.eval.Eval
import com.github.kotlinz.kotlinz.data.eval.EvalMonadRec
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadRec
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.data.state.StateMonadRec
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private sealed class Counter<A>: K1<Counter.T, A> {
    class T
    class Get: Counter<Int>()
    class Add(val n: Int): Counter<Unit>()
}

private fun get(): Free<Counter.T, Int> = Free.liftF(Counter.Get())
private fun add(n: Int): Free<Counter.T, Unit> = Free.liftF(Counter.Add(n))

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

private fun <A> runCounter(free: K1<K1<Free.T, Counter.T>, A>, s: Int = 1): Pair<A, Int> =
    State.narrow(Free.narrow(free).foldMap(CounterToState, object : StateMonadRec<Int> {})).run(s)

private fun countDown(n: Int): Free<Identity.T, Int> =
    if (n == 0) Free.pure(0) else Free.liftF<Identity.T, Int>(Identity(n - 1)) bind { countDown(it) }

class FreeTest : DescribeSpec({
    describe("Free") {
        val monad = object : FreeMonad<Counter.T> {}
        val m = add(2) bind { get() }
        val f = { x: Int -> add(x) bind { get() fmap { it * 10 } } }
        val g = { x: Int -> get() fmap { it + x } }

        context("as Monad") {
            it("should satisfy left identity") { runCounter(monad.bind(f, monad.pure(3))) shouldBe runCounter(f(3)) }
            it("should satisfy right identity") { runCounter(monad.bind({ monad.pure(it) }, m)) shouldBe runCounter(m) }

            it("should satisfy associativity") {
                val left = monad.bind(g, monad.bind(f, m))
                val right = monad.bind({ x: Int -> monad.bind(g, f(x)) }, m)
                runCounter(left) shouldBe runCounter(right)
            }
        }

        context("as Functor") {
            val law = FunctorLaw(monad) { a, b -> runCounter(a) == runCounter(b) }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(m, { it + 1 }, { it.toString() }) }
        }

        context("interpretation") {
            it("should interpret into State") { runCounter(m) shouldBe Pair(3, 3) }

            it("should interpret into a monad without MonadRec") {
                State.narrow(m.foldMap(CounterToState, object : StateMonad<Int> {})).run(1) shouldBe Pair(3, 3)
            }

            it("should support fx") {
                val program = Free.fx<Counter.T, String> {
                    add(5).bind()
                    val x = get().bind()
                    add(x).bind()
                    "x=$x"
                }
                runCounter(program, 0) shouldBe Pair("x=5", 10)
            }

            it("should support fx with multi-shot interpreters") {
                val program = Free.fx<Flip.T, Pair<Boolean, Boolean>> {
                    Pair(Free.liftF<Flip.T, Boolean>(Flip()).bind(), Free.liftF<Flip.T, Boolean>(Flip()).bind())
                }
                ListK.narrow(program.foldMap(FlipToListK, object : ListKMonad {})).value.size shouldBe 4
            }
        }

        context("stack safety") {
            val n = 100_000

            it("should interpret left-nested binds") {
                val program = (1..n).fold(Free.pure<Counter.T, Unit>(Unit)) { acc, _ -> acc bind { add(1) } }
                runCounter(program, 0).second shouldBe n
            }

            it("should interpret right-nested binds into Identity") {
                val identity = NaturalTransformation.identity<Identity.T>()
                Identity.narrow(countDown(n).foldMap(identity, object : IdentityMonadRec {})).value shouldBe 0
            }

            it("should interpret right-nested binds into Eval") {
                val toEval = object : NaturalTransformation<Identity.T, Eval.T> {
                    override fun <A> invoke(fa: K1<Identity.T, A>): Eval<A> = Eval.now(Identity.narrow(fa).value)
                }
                Eval.narrow(countDown(n).foldMap(toEval, object : EvalMonadRec {})).value shouldBe 0
            }
        }
    }
})

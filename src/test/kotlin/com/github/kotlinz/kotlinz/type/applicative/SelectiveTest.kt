package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.const.ConstSelective
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.EitherSelective
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeSelective
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.ValidatedSelective
import com.github.kotlinz.kotlinz.law.SelectiveLaw
import com.github.kotlinz.kotlinz.type.monad.MonadSelective
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <L, R> left(v: L): Either<L, R> = Either.Left(v)
private fun <L, R> right(v: R): Either<L, R> = Either.Right(v)
private val inc = { n: Int -> n + 1 }
private val add = { a: Int -> { b: Int -> a + b } }

class SelectiveTest : DescribeSpec({
    describe("Selective") {
        context("Maybe") {
            val selective = object : MaybeSelective {}

            it("should satisfy selective laws") {
                listOf(left<Int, Int>(1), right(2)).forEach { e ->
                    SelectiveLaw(selective).assertSatisfyingSelectiveLaw(
                        Maybe.Just(left<Int, Int>(2)), Maybe.Just(left<Int, (Int) -> Int>(3)), Maybe.Just(add),
                        Maybe.Just(e), e, Maybe.Just(inc), Maybe.None())
                }
            }

            it("should skip the function on Right") {
                Maybe.select(Maybe.Just(right<Int, Int>(1)), Maybe.None()) shouldBe Maybe.Just(1)
                Maybe.select(Maybe.Just(left<Int, Int>(1)), Maybe.Just(inc)) shouldBe Maybe.Just(2)
                Maybe.select(Maybe.Just(left<Int, Int>(1)), Maybe.None()) shouldBe Maybe.None<Int>()
            }

            it("should only inspect the chosen branch") {
                Maybe.ifS(Maybe.Just(true), Maybe.Just(1), Maybe.None()) shouldBe Maybe.Just(1)
                Maybe.ifS(Maybe.Just(false), Maybe.None(), Maybe.Just(2)) shouldBe Maybe.Just(2)
                selective.whenS(Maybe.Just(false), Maybe.None()) shouldBe Maybe.Just(Unit)
                selective.whenS(Maybe.Just(true), Maybe.None()) shouldBe Maybe.None<Unit>()
            }

            it("should branch on Left and Right") {
                val toString = Maybe.Just { n: Int -> "L$n" }
                val length = Maybe.Just { s: String -> "R${s.length}" }
                selective.branch(Maybe.Just(left<Int, String>(1)), toString, Maybe.None()) shouldBe Maybe.Just("L1")
                selective.branch(Maybe.Just(right<Int, String>("ab")), Maybe.None(), length) shouldBe Maybe.Just("R2")
            }
        }

        context("Either") {
            val selective = object : EitherSelective<String> {}

            it("should satisfy selective laws") {
                SelectiveLaw(selective).assertSatisfyingSelectiveLaw(
                    right<String, Either<Int, Int>>(left(2)), right<String, Either<Int, (Int) -> Int>>(left(3)), right(add),
                    right<String, Either<Int, Int>>(left(1)), left<Int, Int>(1), right(inc), left("e"))
            }

            it("should short-circuit on an outer Left") {
                Either.select(left<String, Either<Int, Int>>("e1"), left<String, (Int) -> Int>("e2")) shouldBe left<String, Int>("e1")
                Either.ifS(right<String, Boolean>(false), left("unused"), right(2)) shouldBe right<String, Int>(2)
            }
        }

        context("Validated") {
            val type = StringMonoid::class.java
            val selective = object : ValidatedSelective<StringMonoid> { override val type = type }
            val err = { s: String -> Validated.invalid<StringMonoid, (Int) -> Int>(StringMonoid(s)) }

            it("should satisfy selective laws") {
                SelectiveLaw(selective).assertSatisfyingSelectiveLaw(
                    Validated.valid(left<Int, Int>(2)), Validated.valid(left<Int, (Int) -> Int>(3)), Validated.valid(add),
                    Validated.valid(right<Int, Int>(1)), left<Int, Int>(1), err("a"), err("b"))
            }

            it("should not accumulate errors from a skipped effect") {
                Validated.select(type, Validated.valid(right<Int, Int>(1)), err("e")) shouldBe Validated.valid(1)
                Validated.select(type, Validated.invalid<StringMonoid, Either<Int, Int>>(StringMonoid("a")), err("b")) shouldBe
                    Validated.invalid(StringMonoid("a"))
                Validated.select(type, Validated.valid(left<Int, Int>(1)), err("b")) shouldBe Validated.invalid(StringMonoid("b"))
            }
        }

        context("Const") {
            val type = StringMonoid::class.java
            val selective = object : ConstSelective<StringMonoid> { override val type = type }
            fun <A> c(s: String) = Const<StringMonoid, A>(StringMonoid(s))

            it("should satisfy selective laws") {
                SelectiveLaw(selective).assertSatisfyingSelectiveLaw(
                    c<Either<Int, Int>>("x"), c<Either<Int, (Int) -> Int>>("y"), c<(Int) -> (Int) -> Int>("z"),
                    c<Either<Int, Int>>("s"), left<Int, Int>(1), c<(Int) -> Int>("f"), c<(Int) -> Int>("g"))
            }

            it("should collect every possible effect") {
                Const.select(type, c<Either<Int, Int>>("cond"), c<(Int) -> Int>("then")) shouldBe c<Int>("condthen")
                selective.ifS(c("c"), c<Int>("t"), c("e")) shouldBe c<Int>("cte")
            }
        }

        context("any Monad") {
            val selective = object : MonadSelective<K1<State.T, Int>>, StateMonad<Int> {}
            val tick = State { s: Int -> Pair(Unit, s + 1) }
            val eqState = { a: K1<*, *>, b: K1<*, *> ->
                (a as State<Int, *>).run(0) == (b as State<Int, *>).run(0)
            }

            it("should satisfy selective laws") {
                SelectiveLaw(selective, eqState).assertSatisfyingSelectiveLaw(
                    selective.pure(left<Int, Int>(2)), selective.pure(left<Int, (Int) -> Int>(3)), selective.pure(add),
                    selective.pure(right<Int, Int>(1)), left<Int, Int>(1), selective.pure(inc), selective.pure { n: Int -> n * 2 })
            }

            it("should run the effect only when the condition holds") {
                State.narrow(selective.whenS(State { s: Int -> Pair(s > 0, s) }, tick)).run(0) shouldBe Pair(Unit, 0)
                State.narrow(selective.whenS(State { s: Int -> Pair(s > 0, s) }, tick)).run(5) shouldBe Pair(Unit, 6)
            }
        }
    }
})

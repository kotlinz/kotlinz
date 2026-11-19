# Automata and folds

Stream processors as plain values. `Moore` and `Mealy` machines consume inputs one at a time and carry their state
inside the next machine; `Fold` is a left fold whose pieces combine into a single pass; `Cokleisli` turns a
comonadic function into an arrow. All four are encoded with a phantom middle parameter (`K3<T, Unit, I, O>`, like
`Function1`) or the comonad tag (`Cokleisli`) so that they fit the two-parameter type classes `Profunctor`,
`Category` and `Arrow` from [Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md).

| Type | Package | Shape | Instances |
|---|---|---|---|
| [`Moore<I, O>`](#moore) | `data.moore` | current output + `(I) -> Moore<I, O>` | `Functor`, `Applicative`, `Comonad`, `Profunctor` |
| [`Mealy<I, O>`](#mealy) | `data.mealy` | `(I) -> Pair<O, Mealy<I, O>>` | `Functor`, `Category`, `Arrow`, `ArrowChoice`, `Profunctor`, `Strong` |
| [`Fold<A, B>`](#fold) | `data.fold` | initial state, step, extract | `Functor`, `Applicative`, `Profunctor` |
| [`Cokleisli<W, A, B>`](#cokleisli) | `data.cokleisli` | `(W<A>) -> B` for a `Comonad<W>` | `Category`, `Arrow`, `Profunctor` |

## Moore

A Moore machine has an `output` that is always observable and a `step` that consumes an input and returns the next
machine. Think of it as a resumable left fold: `run` feeds every input and returns the final output, `scan` returns
every intermediate output (starting with the initial one), and `feedAll` returns the machine itself so you can keep
feeding it later.

The `Applicative` runs machines in lockstep on the same inputs, the `Comonad`'s `extract` is the current output, and
`lmap` / `rmap` / `dimap` adapt the inputs and outputs.

```kotlin
import com.github.kotlinz.kotlinz.data.moore.Moore

val sum = Moore.unfold(0, { s -> s }) { s, i: Int -> s + i }
val count = Moore.unfold(0, { s -> s }) { s, _: Int -> s + 1 }

check(sum.run(listOf(1, 2, 3)) == 6)
check(sum.scan(listOf(1, 2, 3)) == listOf(0, 1, 3, 6))
check((sum lmap String::length).run(listOf("ab", "c")) == 3)

// Resume where you left off.
val afterTwo = sum.feedAll(listOf(1, 2))
check(afterTwo.extract() == 3 && afterTwo.run(listOf(10)) == 13)

// Lockstep: both machines see every input.
val mean = count ap (sum fmap { total -> { n: Int -> if (n == 0) 0.0 else total.toDouble() / n } })
check(mean.run(listOf(2, 4, 9)) == 5.0)

// Comonad: the output a machine would produce after one more input of 100.
check(sum.extend { m -> m.step(100).output }.run(listOf(1, 2)) == 103)
```

## Mealy

A Mealy machine produces an output for every input and moves to its next state; `run` returns the list of
outputs. Mealy machines are stateful stream transducers that compose like functions, so they have `Category` and
`Arrow` instances: `andThen` / `compose` chain machines, `Mealy.arr` lifts a stateless function, `first` /
`second` / `split` / `combine` work on pairs, and the `ArrowChoice` combinators `left` / `right` /
`splitChoice` / `fanin` route `Either` inputs.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.mealy.Mealy

val runningSum = Mealy.unfold(0) { s, i: Int -> Pair(s + i, s + i) }
val index = Mealy.unfold(0) { n, _: Int -> Pair(n, n + 1) }

check(runningSum.run(listOf(1, 2, 3)) == listOf(1, 3, 6))
check((runningSum andThen Mealy.arr { it * 10 }).run(listOf(1, 2)) == listOf(10, 30))
check((index combine runningSum).run(listOf(5, 5)) == listOf(0 to 5, 1 to 10))

// With left(), the machine only advances on Left inputs; Right inputs pass through untouched.
val sumLefts = runningSum.left<String>()
val inputs = listOf(Either.Left<Int, String>(1), Either.Right<Int, String>("x"), Either.Left<Int, String>(2))
check(sumLefts.run(inputs) == listOf(Either.Left<Int, String>(1), Either.Right<Int, String>("x"), Either.Left<Int, String>(3)))

// feedAll returns the outputs and the machine to continue with.
val (outputs, next) = runningSum.feedAll(listOf(1, 2))
check(outputs == listOf(1, 3) && next.run(listOf(4)) == listOf(7))
```

## Fold

`Fold<A, B>` is a composable left fold: an initial state, a step function and a final extraction, with the state
type hidden. Folds are values, so you can build them separately and combine them with `ap` / `zip`; the combined
fold still makes **a single pass** over the input. Ready-made folds are companion extensions in `Folds.kt`:
`Fold.sum()`, `Fold.product()`, `Fold.length()`, `Fold.maximum()`, `Fold.minimum()` (returning `Maybe`),
`Fold.list()`, `Fold.any(p)` and `Fold.all(p)`. Build your own with `Fold.of(initial, step, extract)`.

```kotlin
import com.github.kotlinz.kotlinz.data.fold.Fold
import com.github.kotlinz.kotlinz.data.fold.all
import com.github.kotlinz.kotlinz.data.fold.length
import com.github.kotlinz.kotlinz.data.fold.list
import com.github.kotlinz.kotlinz.data.fold.maximum
import com.github.kotlinz.kotlinz.data.fold.sum
import com.github.kotlinz.kotlinz.data.maybe.Maybe

val average: Fold<Int, Double> = Fold.sum().zip(Fold.length<Int>()) fmap { (s, n) -> s.toDouble() / n }
check(average.run(listOf(2, 4, 9)) == 5.0)

val stats = average.zip(Fold.maximum<Int>()).zip(Fold.all<Int> { it > 0 })
check(stats.run(listOf(3, 1, 2)) == Pair(Pair(2.0, Maybe.Just(3)), true))

check(Fold.list<Int>().scan(listOf(1, 2)) == listOf(listOf(), listOf(1), listOf(1, 2)))
check((Fold.sum() lmap String::length).run(listOf("ab", "cde")) == 5)

val distinct = Fold.of(setOf<String>(), { s, a: String -> s + a }) { it.size }
check(distinct.run(listOf("a", "b", "a")) == 2)
```

The existential state is erased to `Any?` behind the type-safe `Fold.of`, the only constructor. This `Fold` is
unrelated to the read-only optic `com.github.kotlinz.kotlinz.optics.Fold` described in
[Optics](optics.md#getter-and-fold).

## Cokleisli

`Cokleisli<W, A, B>` wraps a function `(W<A>) -> B` for a comonad `W`, the dual of `Kleisli`. Each arrow consumes a
whole context, and composition uses `extend`, so a later arrow sees the context of the earlier arrow's results.
Like `Kleisli` holds its monad, a `Cokleisli` holds its `Comonad<W>`. `Cokleisli.id` extracts the focus and
`Cokleisli.arr` lifts a plain function on the focus.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.cokleisli.Cokleisli
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListComonad
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf

val comonad = object : NonEmptyListComonad {}
// The NonEmptyList comonad focuses on the head; extend sees every suffix.
val total = Cokleisli(comonad) { w: K1<NonEmptyList.T, Int> -> NonEmptyList.narrow(w).let { it.head + it.tail.sum() } }
val firstTwo = Cokleisli(comonad) { w: K1<NonEmptyList.T, Int> -> NonEmptyList.narrow(w).let { listOf(it.head) + it.tail.take(1) } }

check(total.run(nonEmptyListOf(1, 2, 3)) == 6)
// Suffix sums are [6, 5, 3]; firstTwo then sees that context.
check((total andThen firstTwo).run(nonEmptyListOf(1, 2, 3)) == listOf(6, 5))
check((total combine Cokleisli.arr(comonad) { n: Int -> n * 10 }).run(nonEmptyListOf(1, 2)) == Pair(3, 10))
check((total rmap { "sum=$it" }).run(nonEmptyListOf(4)) == "sum=4")
```

`first` runs the arrow over the first components of the whole context and pairs the result with the *focused*
second component, which is the standard Cokleisli arrow structure. See [Comonads](comonads.md) for the comonads you
can plug in.

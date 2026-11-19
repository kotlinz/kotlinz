# Selective

A *selective applicative functor* sits between `Applicative` and `Monad`. With an `Applicative` every effect is
known up front; with a `Monad` the next effect can be any function of earlier results. `Selective` allows exactly
one kind of dependency: the result of one effect decides whether a second, statically known effect runs.

<!-- nocheck -->
```kotlin
interface Selective<T>: Applicative<T> {
  fun <A, B> select(fab: K1<T, Either<A, B>>, fn: K1<T, (A) -> B>): K1<T, B>
  fun <A, B, C> branch(fab: K1<T, Either<A, B>>, fl: K1<T, (A) -> C>, fr: K1<T, (B) -> C>): K1<T, C>
  fun <A> ifS(cond: K1<T, Boolean>, t: K1<T, A>, e: K1<T, A>): K1<T, A>
  fun whenS(cond: K1<T, Boolean>, act: K1<T, Unit>): K1<T, Unit>
}
interface MonadSelective<T>: Selective<T>, Monad<T> // select via bind
```

`select(fab, fn)` runs `fab`. On `Right(b)` it may skip `fn` and returns `b`; on `Left(a)` it runs `fn` and applies
the function to `a`. `branch`, `ifS` and `whenS` are derived from `select`.

| Instance | Behavior of a skipped effect |
|---|---|
| `MaybeSelective`, `EitherSelective<L>` | not run (via `MonadSelective`) |
| `ValidatedSelective<E>` | not run, so its errors are **not** accumulated |
| `ConstSelective<S>` | always "run": every possible effect is collected into the monoid (static analysis) |
| `FreeSelectiveSelective` | recorded in the program; see [Category theory](../category-theory.md) |
| any monad | mix in `MonadSelective`, e.g. `object : MonadSelective<K1<State.T, Int>>, StateMonad<Int> {}` |

`Maybe` and `Either` have `select` and `ifS` on their companions; `Validated.select` and `Const.select` take the
(semi)group class first. The other combinators are on the instance interfaces.

## Maybe and Either

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeSelective

val inc = Maybe.Just { n: Int -> n + 1 }
check(Maybe.select(Maybe.Just(Either.Right<Int, Int>(1)), Maybe.None()) == Maybe.Just(1))
check(Maybe.select(Maybe.Just(Either.Left<Int, Int>(1)), inc) == Maybe.Just(2))
check(Maybe.ifS(Maybe.Just(false), Maybe.None(), Maybe.Just("else")) == Maybe.Just("else"))

val selective = object : MaybeSelective {}
check(selective.whenS(Maybe.Just(false), Maybe.None()) == Maybe.Just(Unit))
check(selective.whenS(Maybe.Just(true), Maybe.None()) == Maybe.None<Unit>())
check(selective.branch(Maybe.Just(Either.Right<Int, String>("ab")), Maybe.None(), Maybe.Just { s: String -> s.length }) == Maybe.Just(2))

check(Either.ifS(Either.Right<String, Boolean>(true), Either.Right(1), Either.Left("unused")) == Either.Right<String, Int>(1))
```

## Validated: skipped effects do not report errors

With `Validated`, an `Invalid` condition or a `Valid(Right)` skips the function effect, so its errors are not
accumulated. Only a `Valid(Left)` condition reaches (and reports) the second effect:

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.validated.Validated

val type = StringMonoid::class.java
val failing = Validated.invalid<StringMonoid, (Int) -> Int>(StringMonoid("fn failed"))

check(Validated.select(type, Validated.valid(Either.Right<Int, Int>(1)), failing) == Validated.valid<StringMonoid, Int>(1))
check(Validated.select(type, Validated.valid(Either.Left<Int, Int>(1)), failing) == Validated.invalid<StringMonoid, Int>(StringMonoid("fn failed")))
check(Validated.select(type, Validated.invalid<StringMonoid, Either<Int, Int>>(StringMonoid("cond failed")), failing) ==
  Validated.invalid<StringMonoid, Int>(StringMonoid("cond failed")))
```

## Const: inspecting every possible effect

`Const` cannot look at values, so it over-approximates: `select` collects the effects of both arguments. This is
what makes selective programs statically analyzable.

```kotlin
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.const.ConstSelective
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid

val log = StringMonoid::class.java
fun <A> effect(name: String) = Const<StringMonoid, A>(StringMonoid(name))

check(Const.select(log, effect<Either<Int, Int>>("cond;"), effect<(Int) -> Int>("then;")) == effect<Int>("cond;then;"))

val selective = object : ConstSelective<StringMonoid> { override val type = log }
check(selective.ifS(effect("cond;"), effect<Int>("then;"), effect("else;")) == effect<Int>("cond;then;else;"))
```

## Any monad: MonadSelective

Every monad is selective: `MonadSelective` implements `select` with `bind`. Mix it into an existing monad
instance to get `whenS` and friends:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.type.monad.MonadSelective

val selective: MonadSelective<K1<State.T, Int>> = object : MonadSelective<K1<State.T, Int>>, StateMonad<Int> {}
val positive = State { s: Int -> Pair(s > 0, s) }
val tick = State { s: Int -> Pair(Unit, s + 1) }

check(State.narrow(selective.whenS(positive, tick)).run(0) == Pair(Unit, 0))
check(State.narrow(selective.whenS(positive, tick)).run(5) == Pair(Unit, 6))
```

For the free selective functor (`FreeSelective`), which records a selective program so it can be both analyzed
and interpreted, see [Category theory](../category-theory.md).

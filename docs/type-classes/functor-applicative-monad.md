# Functor, Applicative, Monad (and Comonad)

This page covers the core chain `Invariant → Functor → Applicative → Monad`, its extensions (`Alternative`,
`MonadPlus`, `MonadZip`, `MonadRec`, `ApplicativeOps`, `MonadOps`) and the dual `Copointed → Comonad`.

## Invariant and Functor

<!-- nocheck -->
```kotlin
interface Invariant<T> { fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B> }
interface Functor<T>: Invariant<T> { fun <A, B> fmap(f: (A) -> B, v: K1<T, A>): K1<T, B> }
```

`Functor.fmap` maps over the value(s) inside a context. Every functor is an invariant functor (`imap` ignores `g`).
`Invariant` on its own is useful for types that both consume and produce values, such as `MonoidOf` and
`SemigroupOf` (see [Monoids](../monoids.md#first-class-dictionaries-monoidof-and-semigroupof)).

Write generic code against `Functor<T>` and pass the instance:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.type.Functor

fun <T> lengths(functor: Functor<T>, v: K1<T, String>): K1<T, Int> = functor.fmap({ it.length }, v)

check(Maybe.narrow(lengths(object : MaybeFunctor {}, Maybe.Just("four"))) == Maybe.Just(4))
check(ListK.narrow(lengths(object : ListKFunctor {}, listKOf("a", "bb"))) == listKOf(1, 2))
```

Most data types expose `fmap` as an infix member:

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe

check((Maybe.Just(1) fmap { it + 1 }) == Maybe.Just(2))
check((Either.Left<String, Int>("boom") fmap { it + 1 }) == Either.Left<String, Int>("boom"))
```

## Pointed and Applicative

<!-- nocheck -->
```kotlin
interface Pointed<T> { fun <A> pure(v: A): K1<T, A> }
interface Applicative<T>: Functor<T>, Pointed<T> { fun <A, B> ap(f: K1<T, (A) -> B>, v: K1<T, A>): K1<T, B> }
```

`Applicative` combines independent computations. Note the argument order: the member form is `value ap function`.
`ApplicativeOps` adds `liftA`, `liftA2`, `liftA3`, which are available on companions:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe

val plusOne = Maybe.Just(1) ap Maybe.pure { x: Int -> x + 1 }
val sum = Maybe.liftA2 { a: Int, b: Int -> a + b }(Maybe.Just(1), Maybe.Just(2))
val missing = Maybe.liftA2 { a: Int, b: Int -> a + b }(Maybe.Just(1), Maybe.None())
val combos = ListK.liftA2 { a: Int, b: String -> "$a$b" }(listKOf(1, 2), listKOf("x", "y"))

check(plusOne == Maybe.Just(2))
check(sum == Maybe.Just(3))
check(missing == Maybe.None<Int>())
check(combos == listKOf("1x", "1y", "2x", "2y"))
```

For curried application of more arguments, `curried()` (in the root package) turns `(A, B, C) -> D` into
`(A) -> (B) -> (C) -> D`.

## Monad

<!-- nocheck -->
```kotlin
interface Monad<T>: Applicative<T> {
  fun <A> join(v: K1<T, K1<T, A>>): K1<T, A>
  fun <A, B> bind(f: (A) -> K1<T, B>, v: K1<T, A>): K1<T, B> = join(fmap(f, v))
}
```

`bind` sequences dependent computations. Instances implement `join` (and usually override `bind`). `MonadOps`
adds `liftM`, `liftM2`, `liftM3`. For longer chains, use [`fx`](../fx.md).

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()
fun reciprocal(n: Int): Maybe<Double> = if (n == 0) Maybe.None() else Maybe.Just(1.0 / n)

check((parse("4") bind ::reciprocal) == Maybe.Just(0.25))
check((parse("0") bind ::reciprocal) == Maybe.None<Double>())
check(Maybe.join(Maybe.Just(Maybe.Just(1))) == Maybe.Just(1))
check(Maybe.liftM2 { a: Int, b: Int -> a * b }(Maybe.Just(3), Maybe.Just(4)) == Maybe.Just(12))
```

## Alternative and MonadPlus

<!-- nocheck -->
```kotlin
interface Alternative<T>: Applicative<T> {
  fun <A> empty(): K1<T, A>
  fun <A> orElse(v1: K1<T, A>, v2: K1<T, A>): K1<T, A>
  fun <A> optional(v: K1<T, A>): K1<T, Maybe<A>>
}
interface MonadPlus<T>: Monad<T>, Alternative<T> { /* mzero = empty, mplus = orElse */ }
```

Instances: `Maybe` (first success wins) and `ListK` (concatenation).

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe

check((Maybe.None<Int>() orElse Maybe.Just(2)) == Maybe.Just(2))
check((Maybe.Just(1) mplus Maybe.Just(2)) == Maybe.Just(1))
check(Maybe.Just(1).optional() == Maybe.Just(Maybe.Just(1)))
check(Maybe.None<Int>().optional() == Maybe.Just(Maybe.None<Int>()))
check((listKOf(1, 2) orElse listKOf(3)) == listKOf(1, 2, 3))
check(ListK.empty<Int>() == listKOf<Int>())
```

Limitation: Haskell's `some` / `many` are intentionally omitted. They are defined by mutual recursion and only
terminate with lazy evaluation, so they would loop forever with Kotlin's strict evaluation.

## MonadZip

`mzip`, `mzipWith` and `munzip` zip two computations element-wise. Instances: `Identity`, `ListK`.

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf

check((listKOf(1, 2, 3) mzip listKOf("a", "b")) == listKOf(Pair(1, "a"), Pair(2, "b")))
check(listKOf(1, 2).mzipWith(listKOf(10, 20)) { a, b -> a + b } == listKOf(11, 22))
check(ListK.munzip(listKOf(Pair(1, "a"), Pair(2, "b"))) == Pair(listKOf(1, 2), listKOf("a", "b")))
```

## MonadRec

`tailRecM(a, f)` runs `f` repeatedly until it returns `Either.Right`, in constant stack space. Instances:
`Identity`, `Eval`, `State`. There are no companion helpers; instantiate the interface. `Free.foldMap` uses
`MonadRec` automatically when the target monad implements it (see [Category theory](../category-theory.md#free-monad)).

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadRec
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadRec

val countdown = object : IdentityMonadRec {}.tailRecM(1_000_000) { n: Int ->
  Identity(if (n == 0) Either.Right<Int, String>("done") else Either.Left<Int, String>(n - 1))
}
check(countdown == Identity("done"))

val ticks = object : StateMonadRec<Int> {}.tailRecM(3) { n: Int ->
  State { s: Int -> Pair(if (n == 0) Either.Right<Int, Unit>(Unit) else Either.Left<Int, Unit>(n - 1), s + 1) }
}
check(State.narrow(ticks).run(0) == Pair(Unit, 4))
```

## Copointed and Comonad

<!-- nocheck -->
```kotlin
interface Copointed<T> { fun <A> extract(v: K1<T, A>): A }
interface Comonad<T>: Copointed<T>, Functor<T> {
  fun <A> duplicate(v: K1<T, A>): K1<T, K1<T, A>>
  fun <A, B> extend(f: (K1<T, A>) -> B, v: K1<T, A>): K1<T, B> = fmap(f, duplicate(v))
}
```

A comonad always holds a "current" value (`extract`) and can compute a new value at every position from its
surroundings (`extend`). Instances: `Identity`, `Eval`, `NonEmptyList`, `Tuple2`, `Function0`, plus the dedicated
comonads in [Comonads](../comonads.md) (`Store`, `Env`, `Traced`, `Cofree`, `Density`, `Day`).

For `NonEmptyList`, `extend` sees every suffix of the list:

```kotlin
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf

val list = nonEmptyListOf(1, 2, 3, 4)
val suffixSums = list.extend { suffix -> NonEmptyList.narrow(suffix).toList().sum() }

check(list.extract() == 1)
check(suffixSums == nonEmptyListOf(10, 9, 7, 4))
```

`Tuple2<E, A>` is the "environment" comonad (`extract` returns the second component):

```kotlin
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2

val labelled = Tuple2("label", 21)
check(labelled.extract() == 21)
check(labelled.extend { t -> Tuple2.narrow(t).first.length * Tuple2.narrow(t).second } == Tuple2("label", 105))
```

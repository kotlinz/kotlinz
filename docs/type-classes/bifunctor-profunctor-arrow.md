# Bifunctor, Profunctor and the Arrow family

These classes abstract over types with two type parameters. They take the tag *before* the last two parameters
(see [Higher-kinded types](../hkt.md#types-with-more-than-one-parameter)).

| Type class | Instances |
|---|---|
| `Bifunctor<T>` | `Either`, `Ior`, `Tuple2`, `Writer` |
| `Profunctor<T, F>` | `Function1`, `Kleisli` |
| `Strong<T, F>` / `Choice<T, F>` | `Kleisli` (`KleisliStrong<F>`, `KleisliChoice<F>`) |
| `Compose<T, F>` / `Category<T, F>` | `Function1`, `Kleisli` |
| `Arrow<T, F>` | `Function1`, `Kleisli` |
| `ArrowChoice<T, F>` / `ArrowApply<T, F>` | `Kleisli` |
| `ArrowLoop<T, F>` | none (see below) |

## Bifunctor

<!-- nocheck -->
```kotlin
interface Bifunctor<T> {
  fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<T, A, B>): K2<T, C, D>
  fun <A, B, C> first(f: (A) -> C, v: K2<T, A, B>): K2<T, C, B>
  fun <A, B, D> second(g: (B) -> D, v: K2<T, A, B>): K2<T, A, D>
}
```

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2

val left: Either<String, Int> = Either.Left("err")
check(left.bimap({ it.uppercase() }, { it + 1 }) == Either.Left<String, Int>("ERR"))
check((Either.Right<String, Int>(1) second { it * 10 }) == Either.Right<String, Int>(10))
check((Ior.both("warn", 1) first { it.length }) == Ior.both(4, 1))
check(Tuple2("a", 1).bimap({ "$it!" }, { it + 1 }) == Tuple2("a!", 2))
```

`Tuple2` has no `first` / `second` members because they would clash with its properties; use `bimap` or
`object : Tuple2Bifunctor {}`.

## Profunctor, Strong, Choice

<!-- nocheck -->
```kotlin
interface Profunctor<T, F> {
  fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<T, F, B, C>): K3<T, F, A, D>
  fun <A, B, C> lmap(f: (A) -> B, v: K3<T, F, B, C>): K3<T, F, A, C>
  fun <A, B, C> rmap(g: (B) -> C, v: K3<T, F, A, B>): K3<T, F, A, C>
}
interface Strong<T, F>: Profunctor<T, F> { fun <A, B, C> first(v: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>> /* + second */ }
interface Choice<T, F>: Profunctor<T, F> { fun <A, B, C> left(v: K3<T, F, A, B>): K3<T, F, Either<A, C>, Either<B, C>> /* + right */ }
```

A profunctor is contravariant in its input and covariant in its output: `dimap` pre-processes the input and
post-processes the output.

```kotlin
import com.github.kotlinz.kotlinz.data.function.Function1

val length = Function1 { s: String -> s.length }
val describe = length.dimap({ n: Int -> "x".repeat(n) }, { len: Int -> "length $len" })
check(describe(3) == "length 3")
check((length lmap { n: Int -> n.toString() })(12345) == 5)
check((length rmap { it * 2 })("abc") == 6)
```

## Category and Arrow

<!-- nocheck -->
```kotlin
interface Compose<T, F> { fun <A, B, C> compose(f: K3<T, F, B, C>, g: K3<T, F, A, B>): K3<T, F, A, C> }
interface Category<T, F>: Compose<T, F> { fun <A> id(): K3<T, F, A, A> }
interface Arrow<T, F>: Category<T, F> {
  fun <A, B> arr(f: (A) -> B): K3<T, F, A, B>
  fun <A, B, C> first(f: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>>
  // second, swap, split (***), combine (&&&) have default implementations
}
```

`Function1` gets these as members:

```kotlin
import com.github.kotlinz.kotlinz.data.function.Function1

val inc = Function1 { n: Int -> n + 1 }
val show = Function1 { n: Int -> "n=$n" }

check((inc andThen show)(1) == "n=2")
check((show compose inc)(1) == "n=2")
check(inc.first<String>()(Pair(1, "x")) == Pair(2, "x"))
check((inc split show)(Pair(1, 2)) == Pair(2, "n=2"))
check((inc combine show)(5) == Pair(6, "n=5"))
check(Function1.id<Int>()(7) == 7)
```

### Kleisli arrows

`Kleisli<F, A, B>` wraps `(A) -> K1<F, B>` together with the `Monad<F>` used to compose it. It is an instance of
`Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `Profunctor`, `Strong` and `Choice`.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.kleisli.Kleisli
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad

val monad = object : MaybeMonad {}
val parse = Kleisli(monad) { s: String -> s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None() }
val reciprocal = Kleisli(monad) { n: Int -> if (n == 0) Maybe.None() else Maybe.Just(1.0 / n) }

val parseThenInvert = parse.compose(reciprocal)
check(parseThenInvert.run("4") == Maybe.Just(0.25))
check(parseThenInvert.run("0") == Maybe.None<Double>())

val either = parse.fanin(Kleisli.arr(monad) { n: Int -> n })
check(either.run(Either.Left("12")) == Maybe.Just(12))
check(either.run(Either.Right(3)) == Maybe.Just(3))
```

Note that `Kleisli.compose(g)` means "this, then `g`" (it calls `Category.compose(g, this)`), which is the
opposite argument order from `Function1.compose`.

### ArrowChoice and ArrowApply

`ArrowChoice` adds `left`, `right`, `splitChoice` (`+++`) and `fanin` (`|||`) to route `Either` inputs.
`ArrowApply` adds `app`, an arrow that takes an arrow and its input. Both are provided for `Kleisli`.

### ArrowLoop

`ArrowLoop.loop` feeds part of the output back as input. It needs a lazily evaluated feedback value (for `Kleisli`
it would require `MonadFix`), which strict Kotlin data types cannot provide, so the interface is defined but
**no instances are provided**.

# Bifunctor, Profunctor and the Arrow family

These classes abstract over types with two type parameters. They take the tag *before* the last two parameters
(see [Higher-kinded types](../hkt.md#types-with-more-than-one-parameter)).

| Type class | Instances |
|---|---|
| `Bifunctor<T>` | `Either`, `Ior`, `Tuple2`, `Validated`, `Writer` |
| `Bifoldable<T>` / `Bitraversable<T>` | `Either`, `Ior`, `Tuple2`, `Validated` (see [Foldable and Traversable](foldable-traversable.md#bifoldable-and-bitraversable)) |
| `Profunctor<T, F>` | `Function1`, `Kleisli`, `Star`, `Costar`, `Forget`, `Tagged`, `Procompose`, `Cayley`, `Tambara`, `Cokleisli`, `Mealy`, `Moore`, `Fold` |
| `Strong<T, F>` | `Function1`, `Kleisli`, `Star`, `Forget`, `Mealy`, `Procompose`, `Cayley`, `Tambara` |
| `Choice<T, F>` | `Function1`, `Kleisli`, `Star`, `Forget` (needs a monoid), `Tagged` |
| `Closed<T, F>` | `Function1`, `Star` (over a `Distributive`), `Costar`, `Tagged` |
| `Traversing<T, F>` | `Function1`, `Star` (over an `Applicative`), `Forget` (needs a monoid) |
| `Cochoice<T, F>` / `Costrong<T, F>` | `Function1`, `Forget` / `Tagged` |
| `Compose<T, F>` / `Category<T, F>` | `Function1`, `Kleisli`, `Cokleisli`, `Mealy`, `FreeCategory`, `FreeArrow` |
| `Arrow<T, F>` | `Function1`, `Kleisli`, `Cokleisli`, `Mealy`, `FreeArrow` |
| `ArrowChoice<T, F>` | `Kleisli`, `Mealy` |
| `ArrowApply<T, F>` | `Kleisli` |
| `ArrowLoop<T, F>` | `Kleisli` over a `MonadFix` (see [below](#arrowloop)) |

`Cokleisli` and the `Mealy` / `Moore` machines and composable `Fold`s are described in [Automata](../automata.md);
`FreeCategory` and `FreeArrow` in [Category theory](../category-theory.md); the profunctor-encoded optics built on
`Strong`, `Choice` and `Traversing` in [Optics](../optics.md).

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

### More profunctor classes

<!-- nocheck -->
```kotlin
interface Closed<T, F>: Profunctor<T, F> { fun <A, B, X> closed(v: K3<T, F, A, B>): K3<T, F, (X) -> A, (X) -> B> }
interface Traversing<T, F>: Strong<T, F>, Choice<T, F> {
  fun <S, R, A, B> wander(traversal: Wander<S, R, A, B>, v: K3<T, F, A, B>): K3<T, F, S, R>
  fun <G, A, B> traverse(traversable: Traversable<G>, v: K3<T, F, A, B>): K3<T, F, K1<G, A>, K1<G, B>>
}
interface Cochoice<T, F>: Profunctor<T, F> { fun <A, B, C> unleft(v: K3<T, F, Either<A, C>, Either<B, C>>): K3<T, F, A, B> /* + unright */ }
interface Costrong<T, F>: Profunctor<T, F> { fun <A, B, C> unfirst(v: K3<T, F, Pair<A, C>, Pair<B, C>>): K3<T, F, A, B> /* + unsecond */ }
```

`Closed` lifts an arrow to work on functions, `Traversing` lifts it to work on every element of a traversable
structure (`Wander` is the van Laarhoven traversal, encoded as an interface with a generic method because Kotlin
has no rank-2 types), and `Cochoice` / `Costrong` are the duals of `Choice` / `Strong`: they *remove* an `Either` or
a pair component (for `Function1`, `unleft` loops while the result is a `Right`).

### Profunctor data types

| Type | Wraps | Typical use |
|---|---|---|
| `Star<F, A, B>` | `(A) -> K1<F, B>` | effectful functions (like `Kleisli`, but needs only a `Functor` / `Applicative`) |
| `Costar<F, A, B>` | `(K1<F, A>) -> B` | functions that consume a whole structure (folds, aggregations) |
| `Forget<R, A, B>` | `(A) -> R` | getters and folds: ignores the output type `B` |
| `Tagged<A, B>` | a value of `B` | review / construction: ignores the input type `A` |
| `Procompose<P, Q, A, C>` | `Q<A, X>` then `P<X, C>` | composing two different profunctors |
| `Cayley<F, P, A, B>` | `F<P<A, B>>` | a profunctor under a functor |
| `Tambara<P, A, B>` | `P<Pair<A, C>, Pair<B, C>>` for every `C` | the free `Strong` profunctor over `P` |

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.forget.Forget
import com.github.kotlinz.kotlinz.data.forget.ForgetTraversing
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Compose
import com.github.kotlinz.kotlinz.data.function.Function1Strong
import com.github.kotlinz.kotlinz.data.listk.ListKTraversable
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.data.procompose.Procompose
import com.github.kotlinz.kotlinz.data.procompose.collapse
import com.github.kotlinz.kotlinz.data.star.Star
import com.github.kotlinz.kotlinz.data.star.StarTraversing
import com.github.kotlinz.kotlinz.data.tagged.Tagged
import com.github.kotlinz.kotlinz.data.tagged.TaggedChoice
import com.github.kotlinz.kotlinz.data.tambara.Tambara

val list = object : ListKTraversable {}

// Star: traverse an effectful function over a list
val half = Star { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None() }
val starTraversing = object : StarTraversing<Maybe.T> { override val applicative = object : MaybeApplicative {} }
check(Star.narrow(starTraversing.traverse(list, half))(listKOf(2, 4)) == Maybe.Just(listKOf(1, 2)))
check(Star.narrow(starTraversing.traverse(list, half))(listKOf(2, 3)) == Maybe.None<Int>())

// Forget: combine the results for every focus with a monoid
val double = Forget<Int, Int, Int> { it * 2 }
val sumAll = object : ForgetTraversing<Int> { override val monoid = MonoidOf(0) { a: Int, b: Int -> a + b } }
check(Forget.narrow(sumAll.traverse(list, double))(listKOf(1, 2, 3)) == 12)

// Tagged: only the output matters
check(object : TaggedChoice {}.left<Int, Int, String>(Tagged(7)).value == Either.Left<Int, String>(7))

// Procompose: the inner arrow runs first
val composed = Procompose.of(Function1 { n: Int -> n * 2 }, Function1 { n: Int -> n + 3 })
check(Function1.narrow(composed.collapse(object : Function1Compose {}))(1) == 8)

// Tambara: a Strong arrow that works for every context type
val inc = Tambara.of(object : Function1Strong {}, Function1 { n: Int -> n + 1 })
check(Function1.narrow(inc.run<String>())(Pair(1, "x")) == Pair(2, "x"))
check(Function1.narrow(inc.untambara(object : Function1Strong {}))(41) == 42)
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

<!-- nocheck -->
```kotlin
interface ArrowLoop<T, F>: Arrow<T, F> {
  fun <A, B, D> loop(f: K3<T, F, Pair<A, () -> D>, Pair<B, D>>): K3<T, F, A, B>
}
```

`ArrowLoop.loop` feeds the second output `D` back as the second input. Kotlin is strict, so the fed-back value
arrives as a thunk `() -> D`: the arrow may capture it (for example in a lazily linked structure) but must not force
it before producing its output. The instance is `KleisliArrowLoop<F>` for a [`MonadFix`](functor-applicative-monad.md#monadfix)
`F` (the feedback is the `mfix` of the arrow's output), with the companion helper `Kleisli.loop(monadFix, f)`.

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonadFix
import com.github.kotlinz.kotlinz.data.kleisli.Kleisli

class Stream(val head: Int, val tail: () -> Stream) {
  fun take(n: Int): List<Int> = if (n == 0) emptyList() else listOf(head) + tail().take(n - 1)
}

val monad = object : IdentityMonadFix {}
// The stream refers to itself through the fed-back thunk.
val repeat = Kleisli.loop(monad, Kleisli(monad) { p: Pair<Int, () -> Stream> ->
  val stream = Stream(p.first, p.second)
  Identity(Pair(stream, stream))
})
check(Identity.narrow(repeat.run(7)).value.take(3) == listOf(7, 7, 7))
```

Forcing the thunk inside the arrow (before the output exists) throws `IllegalStateException`, the strict
counterpart of a diverging `loop` in Haskell.

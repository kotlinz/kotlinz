# Higher-kinded types and library conventions

Kotlin cannot abstract over a type constructor such as `List` or `Maybe` directly: you can write
`Functor<List<A>>`, but not `Functor<List>`. Kotlinz works around this with the
*lightweight higher-kinded polymorphism* encoding (also used by [highj](https://github.com/DanielGronau/highj)
and early versions of Arrow). This page explains the encoding and the conventions that every other guide relies on.

## `K1` and the `T` tag

The core of the encoding lives in `com.github.kotlinz.kotlinz.Kinds`:

<!-- nocheck -->
```kotlin
interface K1<T, A>
interface K2<T, A, B>: K1<K1<T, A>, B>
interface K3<T, A, B, C>: K1<K2<T, A, B>, C>
interface K4<T, A, B, C, D>: K1<K3<T, A, B, C>, D>
interface K5<T, A, B, C, D, E>: K1<K4<T, A, B, C, D>, E>
```

`K1<F, A>` reads as "`F` applied to `A`". Every data type declares a nested marker class `T` that stands for the
type constructor itself, and implements `K1<Its.T, A>`:

<!-- nocheck -->
```kotlin
sealed class Maybe<A>: K1<Maybe.T, A> {
  class T                       // the "Maybe" type constructor
  class None<A>: Maybe<A>()
  class Just<A>(val value: A): Maybe<A>()
}
```

A type class is then an interface over the tag, so `Functor<Maybe.T>` is a functor for `Maybe`:

<!-- nocheck -->
```kotlin
interface Functor<T> {
  fun <A, B> fmap(f: (A) -> B, v: K1<T, A>): K1<T, B>
}
```

## `narrow`: getting the concrete type back

Generic code only sees `K1<Maybe.T, A>`. Every data type has a `narrow` function on its companion that performs the
(unchecked but safe-by-construction) downcast back to the concrete type:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.type.Functor

fun <T> doubleAll(functor: Functor<T>, v: K1<T, Int>): K1<T, Int> = functor.fmap({ it * 2 }, v)

val doubled: K1<Maybe.T, Int> = doubleAll(object : MaybeFunctor {}, Maybe.Just(21))
val concrete: Maybe<Int> = Maybe.narrow(doubled)
check(concrete == Maybe.Just(42))
```

The cast is only safe because `Maybe` is the *only* class that implements `K1<Maybe.T, A>`. Never implement another
type's `K1<X.T, ...>` yourself.

## Types with more than one parameter

A type with several parameters is partially applied by nesting: `Either<L, R>` implements `K2<Either.T, L, R>`, which
is `K1<K1<Either.T, L>, R>`. The "type constructor" seen by `Functor` / `Monad` is therefore `K1<Either.T, L>`, with
the *last* parameter varying:

| Data type | Declared as | Type-class tag (last parameter free) |
|---|---|---|
| `Maybe<A>` | `K1<Maybe.T, A>` | `Maybe.T` |
| `Either<L, R>` | `K2<Either.T, L, R>` | `K1<Either.T, L>` |
| `State<S, A>` | `K2<State.T, S, A>` | `K1<State.T, S>` |
| `EitherT<F, E, A>` | `K3<EitherT.T, F, E, A>` | `K2<EitherT.T, F, E>` |
| `RWS<R, W, S, A>` | `K4<RWS.T, R, W, S, A>` | `K3<RWS.T, R, W, S>` |
| `Function1<A, B>` | `K3<Function1.T, Unit, A, B>` | `K2<Function1.T, Unit, A>` (phantom `Unit`, see below) |

Two-parameter type classes (`Bifunctor`, `Profunctor`, `Category`, `Arrow`) take the tag *before* the last two
parameters instead: `Bifunctor<Either.T>` works on `K2<Either.T, A, B>`, and `Arrow<Kleisli.T, F>` works on
`K3<Kleisli.T, F, A, B>`. `Function1` is encoded as `K3<Function1.T, Unit, A, B>` with a phantom `Unit` purely so
that it fits the `Arrow<T, F>` shape.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.EitherMonad
import com.github.kotlinz.kotlinz.type.monad.Monad

val eitherMonad: Monad<K1<Either.T, String>> = object : EitherMonad<String> {}
val result = Either.narrow(eitherMonad.bind({ x: Int -> Either.Right<String, Int>(x + 1) }, Either.Right(1)))
check(result == Either.Right<String, Int>(2))
```

## Instance interfaces vs. companion and member helpers

Every type-class instance is a **public interface** named `<DataType><TypeClass>`, for example `MaybeFunctor`,
`EitherMonad<L>`, `ListKTraversable`, `StateMonadState<S>`. Implementations are default methods, so you obtain an
instance with an anonymous object:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf

val listMonad = object : ListKMonad {}
val pairs = ListK.narrow(listMonad.bind({ x: Int -> listKOf(x, x * 10) }, listKOf(1, 2)))
check(pairs == listKOf(1, 10, 2, 20))
```

Some `<DataType>Applicative` interfaces (for example `EitherApplicative`, `IdentityApplicative`, `StateApplicative`)
do not extend the matching `<DataType>Functor`, so they cannot be instantiated on their own. Use the `Monad`
instance (`object : EitherMonad<String> {}`) wherever an `Applicative` is expected.

Pass instances explicitly whenever a function is generic in the type constructor (`traverse`, `Free.foldMap`,
transformers, `Compose`, `Kleisli`, ...).

For day-to-day use, data types also expose the most common operations directly, already narrowed:

- **members** (often `infix`): `fmap`, `ap`, `bind`, `foldl`, `foldr`, `foldMap`, `traverse`, `orElse`,
  `handleErrorWith`, `extract`, `extend`, ...
- **companion functions**: `pure`, `join`, `fx`, `liftA`/`liftA2`/`liftA3`, `liftM`/`liftM2`/`liftM3`, `sequence`,
  `empty`, `raiseError`, and type-specific constructors such as `State.get()` or `Reader.ask()`.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

val viaMembers = Maybe.Just(2) bind { x -> Maybe.Just(x * 3) } fmap { it + 1 }
val viaLift = Maybe.liftA2 { a: Int, b: Int -> a + b }(Maybe.Just(1), Maybe.Just(2))
check(viaMembers == Maybe.Just(7))
check(viaLift == Maybe.Just(3))
```

Not every instance has a helper. If a member or companion function is missing (for example `MonadRec.tailRecM`, or
`Kleisli`'s `Strong` instance), instantiate the interface directly.

## Passing monoids and semigroups: `Class<M>`

Kotlin has no implicit instance resolution, so `Monoid` / `Semigroup` instances are looked up *by class*.
Algebraic structures are encoded as F-bounded interfaces implemented by wrapper classes with a public no-argument
constructor:

<!-- nocheck -->
```kotlin
interface Monoid<A, Self: Monoid<A, Self>>: Semigroup<A, Self> {
  fun mzero(): Self
  companion object {
    fun <A: Monoid<*, A>> op(type: Class<A>, a: A, b: A): A
    fun <A: Monoid<*, A>> mzero(type: Class<A>): A
  }
}
```

Anything that needs a monoid takes a `Class<M>` and creates an instance reflectively via the no-argument constructor:
`Writer`, `Tuple2`, `Const`, `Traced`, `RWS`, `WriterT`, `OpDecidable`, `foldMap`, `FreeAp.analyze`, and the
semigroup-based `Validated` and `Ior`.

```kotlin
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.monoid.toSumMonoid

val total = listKOf(1, 2, 3, 4).foldMap(SumMonoid::class.java) { it.toSumMonoid() }
check(total == SumMonoid(10))
```

For generic monoid classes such as `FirstMonoid<A>` or `MaxMonoid<A>`, `FirstMonoid::class.java` is a raw
`Class<FirstMonoid<*>>`, so an unchecked cast is required:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.MaxMonoid
import com.github.kotlinz.kotlinz.data.monoid.toMaxMonoid

@Suppress("UNCHECKED_CAST")
val maxType = MaxMonoid::class.java as Class<MaxMonoid<Int>>
val max = listKOf(3, 9, 4).foldMap(maxType) { Maybe.Just(it).toMaxMonoid() }
check(max == MaxMonoid(Maybe.Just(9)))
```

If you need a monoid as a plain value instead of a class, see `MonoidOf` / `SemigroupOf` in the
[monoids guide](monoids.md).

## Instance values: `Eq`, `Order`, `Show`, `Hash`

`Eq`, `Order`, `Show` and `Hash` take a third approach. They are ordinary interfaces over a proper type (`Eq<A>`,
not `Eq<T>` over a tag), and an instance is a **value** that you pass explicitly, much like a `Comparator`:

- primitive instances are companion extension properties: `Int.eq`, `String.order`, `Long.hash`, `Boolean.show`;
- data types derive instances from their element instances with companion factories: `Maybe.eq(Int.eq)`,
  `Either.order(String.order, Int.order)`, `ListK.show(Int.show)`;
- `Eq.by`, `Order.by`, `Show.by` and `Hash.by` build ad-hoc instances, and `Eq.fromEquals()`,
  `Order.fromComparable()`, `Show.fromToString()`, `Hash.fromHashCode()` delegate to the JVM methods.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.eq
import com.github.kotlinz.kotlinz.data.maybe.order
import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.eq.eq
import com.github.kotlinz.kotlinz.type.order.order

val caseInsensitive = Eq.by<String> { a, b -> a.equals(b, ignoreCase = true) }
val maybeOrder = Maybe.order(Int.order)

check(Maybe.eq(Int.eq).eqv(Maybe.Just(1), Maybe.Just(1)))
check(Maybe.eq(caseInsensitive).eqv(Maybe.Just("Kotlin"), Maybe.Just("KOTLIN")))
check(maybeOrder.lt(Maybe.None(), Maybe.Just(0)))
check(listOf(Maybe.Just(2), Maybe.None(), Maybe.Just(1)).sortedWith(maybeOrder.toComparator()) ==
  listOf(Maybe.None(), Maybe.Just(1), Maybe.Just(2)))
```

Compare this with `Semigroup` / `Monoid`, where the *values themselves* are wrapper objects (`SumMonoid(3)`)
and the instance is found through a `Class<M>`. The instance-value style needs no reflection and composes freely,
but every call site has to pass the instance. See [Eq, Order, Show and Hash](type-classes/eq-order-show-hash.md)
for the full list of instances.

## Known limitations of the encoding

- `narrow` is an unchecked cast. It is safe only as long as each tag has exactly one implementing class.
- Type parameters of `K1` are invariant, so `Maybe.Just<Int>` is not a `K1<Maybe.T, Int>` in every position where
  Kotlin cannot infer the expected type. Annotate the type argument (`listKOf<K1<Maybe.T, Int>>(...)`) or widen with
  `as Maybe<Int>` when inference fails.
- Generic signatures get verbose (`K1<K1<Either.T, L>, R>`); prefer the narrowed member and companion helpers in
  application code.
- Monoid lookup through `Class<M>` uses reflection and requires a public no-argument constructor.

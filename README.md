# Kotlinz

[![](https://jitpack.io/v/kotlinz/kotlinz.svg)](https://jitpack.io/#kotlinz/kotlinz)
[![CI](https://github.com/kotlinz/kotlinz/actions/workflows/ci.yml/badge.svg)](https://github.com/kotlinz/kotlinz/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/kotlinz/kotlinz/branch/master/graph/badge.svg)](https://codecov.io/gh/kotlinz/kotlinz)

**Typed functional programming for Kotlin: higher-kinded type classes, data types, monad transformers and
category-theory constructs, with no dependencies.**

**[Documentation website](https://kotlinz.github.io/kotlinz/)** · **[API reference](https://kotlinz.github.io/kotlinz/api/)**

- Higher-kinded types via the lightweight `K1<F, A>` encoding, so `Functor`, `Monad`, `Traversable`, ... are
  ordinary interfaces you can write generic code against.
- A broad type-class hierarchy: from `Functor` / `Applicative` / `Monad` and `Foldable` / `Traversable` to
  MTL-style classes, `Comonad`, `Profunctor`, `Arrow`, `Decidable`, `Representable` and `Adjunction`.
- Familiar data types (`Maybe`, `Either`, `Try`, `Validated`, `IO`, `State`, `Reader`, `Writer`, ...), monad
  transformers, comonads, `Free` / `FreeAp` / `Cofree`, Kan extensions and recursion schemes.
- Do-notation (`fx`) built on Kotlin coroutines.
- Type-class instances are checked against their laws in the test suite.

## Installation

### Gradle (Kotlin DSL)

<!-- nocheck -->
```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kotlinz:kotlinz:$version")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.kotlinz:kotlinz:$version"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.kotlinz</groupId>
    <artifactId>kotlinz</artifactId>
    <version>$version</version>
</dependency>
```

## Quick start

Data types expose their most common operations as (mostly `infix`) members and companion functions:

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

val doubled = parse("21") fmap { it * 2 }
val sum = Maybe.liftA2 { a: Int, b: Int -> a + b }(parse("1"), parse("2"))
val chained = parse("8") bind { n -> if (n > 0) Maybe.Just(100 / n) else Maybe.None() }

check(doubled == Maybe.Just(42))
check(sum == Maybe.Just(3))
check(chained == Maybe.Just(12))
check(parse("x").getOrElse(0) == 0)
```

Type-class instances are public interfaces (`MaybeMonad`, `ListKTraversable`, `StateMonadState<S>`, ...), so
generic code takes them as parameters and `narrow` recovers the concrete type:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.type.Functor

fun <F> describe(functor: Functor<F>, v: K1<F, Int>): K1<F, String> = functor.fmap({ "#$it" }, v)

check(Maybe.narrow(describe(object : MaybeFunctor {}, Maybe.Just(1))) == Maybe.Just("#1"))
check(ListK.narrow(describe(object : ListKFunctor {}, listKOf(1, 2))) == listKOf("#1", "#2"))
```

See [Higher-kinded types and conventions](docs/hkt.md) for how the encoding works.

## Highlights

### Monad comprehension with `fx`

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.state.State

val nextId = State.fx<Int, String> {
  val id = State.get<Int>().bind()
  State.put(id + 1).bind()
  "user-$id"
}
check(nextId.run(7) == Pair("user-7", 8))

val pairs = ListK.fx {
  val a = listKOf(1, 2).bind()
  val b = listKOf("x", "y").bind()
  "$a$b"
}
check(pairs == listKOf("1x", "1y", "2x", "2y"))
```

`fx` is available on the companions of most monads; multi-shot monads such as `ListK` use `fxReplay`
automatically. See [fx](docs/fx.md) for the details and caveats.

### Traversable

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()
val maybe = object : MaybeApplicative {}

check(Maybe.narrow(listKOf("1", "2", "3").traverse(maybe, ::parse)) == Maybe.Just(listKOf(1, 2, 3)))
check(Maybe.narrow(listKOf("1", "oops").traverse(maybe, ::parse)) == Maybe.None<ListK<Int>>())
```

### Accumulating errors with `Validated`

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.validated.Validated

data class User(val name: String, val age: Int)

fun name(s: String): Validated<ListMonoid, String> =
  if (s.isNotBlank()) Validated.valid(s) else Validated.invalid(ListMonoid(listOf("name is blank")))
fun age(n: Int): Validated<ListMonoid, Int> =
  if (n >= 0) Validated.valid(n) else Validated.invalid(ListMonoid(listOf("age is negative")))

val user = Validated.liftA2(ListMonoid::class.java, ::User)

check(user(name("ann"), age(30)) == Validated.valid<ListMonoid, User>(User("ann", 30)))
check(user(name(""), age(-1)) == Validated.invalid<ListMonoid, User>(ListMonoid(listOf("name is blank", "age is negative"))))
```

### Monad transformers

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.eithert.EitherT
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad

val io = object : IOMonad {}
fun readPort(raw: String): EitherT<IO.T, String, Int> =
  EitherT(io, IO { raw.toIntOrNull()?.let { Either.Right<String, Int>(it) } ?: Either.Left("invalid port: $raw") })

val url = EitherT.fx<IO.T, String, String>(io) {
  val port = readPort("8080").bind()
  val host = EitherT.lift<IO.T, String, String>(io, IO { "localhost" }).bind()
  "$host:$port"
}

check(IO.narrow(url.run).unsafeRun() == Either.Right<String, String>("localhost:8080"))
check(IO.narrow(readPort("x").run).unsafeRun() == Either.Left<String, Int>("invalid port: x"))
```

### Free monads

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.free.Free
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadRec
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

sealed class Counter<A> : K1<Counter.T, A> {
  class T
  class Add(val n: Int) : Counter<Unit>()
  object Current : Counter<Int>()
}

fun add(n: Int): Free<Counter.T, Unit> = Free.liftF(Counter.Add(n))
val current: Free<Counter.T, Int> = Free.liftF(Counter.Current)

val program: Free<Counter.T, String> = Free.fx {
  add(2).bind()
  add(3).bind()
  "total=${current.bind()}"
}

val toState = object : NaturalTransformation<Counter.T, K1<State.T, Int>> {
  @Suppress("UNCHECKED_CAST")
  override fun <A> invoke(fa: K1<Counter.T, A>): State<Int, A> = when (val op = fa as Counter<A>) {
    is Counter.Add -> State { s: Int -> Pair(Unit as A, s + op.n) }
    is Counter.Current -> State { s: Int -> Pair(s as A, s) }
  }
}

check(State.narrow(program.foldMap(toState, object : StateMonadRec<Int> {})).run(0) == Pair("total=5", 5))
```

## Coverage

| Category | What is included |
|---|---|
| Functor family | `Invariant`, `Functor`, `Pointed`, `Applicative`, `Monad`, `Alternative`, `MonadPlus`, `MonadZip`, `MonadRec`, `Copointed`, `Comonad` |
| Folding | `Foldable`, `Traversable`, `Distributive`, `Representable` |
| Effects (MTL) | `ApplicativeError`, `MonadError`, `MonadReader`, `MonadState`, `MonadWriter` |
| Two-parameter | `Bifunctor`, `Profunctor`, `Strong`, `Choice`, `Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `ArrowLoop` |
| Contravariant | `Contravariant`, `Divisible`, `Decidable` |
| Algebra | `Semigroup`, `Monoid`, `Group`, `Band`, `Semiring`, `Lattice`, `BoundedLattice`, 15 monoid instances, `MonoidOf`, `SemigroupOf` |
| Data types | `Identity`, `Maybe`, `Either`, `Try`, `Validated`, `Ior`, `Eval`, `IO`, `Reader`, `Writer`, `State`, `RWS`, `Cont`, `ListK`, `NonEmptyList`, `Tuple2`, `Twin`, `Const`, `Function0`, `Function1`, `Kleisli`, `Predicate`, `Comparison`, `Equivalence`, `Op`, `Compose`, `Product`, `Coproduct` |
| Transformers | `MaybeT`, `EitherT`, `ReaderT`, `StateT`, `WriterT` |
| Comonads | `Store`, `Env`, `Traced`, `Cofree`, `Density`, `Day` |
| Category theory | `NaturalTransformation`, `Adjunction`, `Yoneda`, `Coyoneda`, `Codensity`, `Ran`, `Lan`, `Day`, `Free`, `FreeAp`, `Fix` with `cata` / `ana` / `hylo` / `para` / `apo` |

Core data types × type classes (✓ = instance provided; * = takes a `Monoid` / `Semigroup` class):

| | Functor | Applicative | Monad | Traversable | Comonad | Other |
|---|---|---|---|---|---|---|
| `Identity` | ✓ | ✓ | ✓ | ✓ | ✓ | `MonadZip`, `MonadRec`, `Representable` |
| `Maybe` | ✓ | ✓ | ✓ | ✓ | | `MonadPlus` |
| `Either` | ✓ | ✓ | ✓ | ✓ | | `MonadError`, `Bifunctor` |
| `Try` | ✓ | ✓ | ✓ | ✓ | | `MonadError` |
| `Validated` * | ✓ | ✓ | | | | error accumulation |
| `Ior` * | ✓ | ✓ | ✓ | | | `Bifunctor` |
| `Eval` | ✓ | ✓ | ✓ | | ✓ | `MonadRec` |
| `IO` | ✓ | ✓ | ✓ | | | `MonadError` |
| `Reader` | ✓ | ✓ | ✓ | | | `MonadReader`, `Representable` |
| `Writer` * | ✓ | ✓ | ✓ | | | `MonadWriter`, `Bifunctor` |
| `State` | ✓ | ✓ | ✓ | | | `MonadState`, `MonadRec` |
| `ListK` | ✓ | ✓ | ✓ | ✓ | | `MonadPlus`, `MonadZip` |
| `NonEmptyList` | ✓ | ✓ | ✓ | ✓ | ✓ | |

The complete matrix is in [Type classes](docs/type-classes/README.md#coverage).

## Documentation

**Website: [kotlinz.github.io/kotlinz](https://kotlinz.github.io/kotlinz/)**, with guides, search and the API
reference in one place.

- **Guides**: [Getting started](https://kotlinz.github.io/kotlinz/getting-started) (sources in [docs/](docs/README.md))
  - [Higher-kinded types and conventions](docs/hkt.md)
  - [Type classes](docs/type-classes/README.md)
  - [Data types](docs/data-types.md)
  - [Monad comprehension (`fx`)](docs/fx.md)
  - [Monad transformers](docs/transformers.md)
  - [Monoids and algebraic structures](docs/monoids.md)
  - [Comonads](docs/comonads.md)
  - [Category theory](docs/category-theory.md)
- **API reference**: [kotlinz.github.io/kotlinz/api](https://kotlinz.github.io/kotlinz/api/)

The website is built with [VitePress](https://vitepress.dev/) from `docs/`. To preview it locally, run
`npm ci && npm run docs:dev`.

## Related

- [Scalaz](https://github.com/scalaz/scalaz)
- [Swiftz](https://github.com/typelift/Swiftz)
- [highj](https://github.com/DanielGronau/highj)

## License

See [LICENSE](LICENSE).

## Author

[@unhappychoice](https://unhappychoice.com)

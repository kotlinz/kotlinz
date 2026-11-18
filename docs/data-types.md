# Data types

All data types live in `com.github.kotlinz.kotlinz.data.<name>` (for example `data.maybe.Maybe`,
`data.trying.Try`, `data.listk.ListK`). The full instance matrix is in [Type classes](type-classes/README.md#coverage);
this page shows how to construct and use each type. Monad transformers, comonads and category-theory constructs
have their own guides: [Monad transformers](transformers.md), [Comonads](comonads.md),
[Category theory](category-theory.md).

| Group | Types |
|---|---|
| [Optional values and errors](#optional-values-and-errors) | `Identity`, `Maybe`, `Either`, `Try`, `Validated`, `Ior` |
| [Laziness and effects](#laziness-and-effects) | `Eval`, `IO`, `Function0` |
| [Environment, output and state](#environment-output-and-state) | `Reader`, `Writer`, `State`, `RWS`, `Cont` |
| [Collections and products](#collections-and-products) | `ListK`, `NonEmptyList`, `Tuple2`, `Twin`, `Const` |
| [Functions](#functions) | `Function1`, `Kleisli`, `Predicate`, `Comparison`, `Equivalence`, `Op` |
| [Functor combinators](#functor-combinators) | `Compose`, `Product`, `Coproduct` |

Unless noted otherwise, every monad below supports `fmap` / `ap` / `bind` as members and `pure`, `join`, `fx`,
`liftA*` and `liftM*` on its companion.

## Optional values and errors

### Identity

The trivial wrapper. Useful as the base monad of a transformer and as the simplest instance of almost every class
(`Monad`, `Traversable`, `Comonad`, `MonadZip`, `MonadRec`, `Distributive`, `Representable`).

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity

check((Identity(20) bind { Identity(it + 1) } fmap { it * 2 }) == Identity(42))
check(Identity(1).extract() == 1)
```

### Maybe

`Maybe.Just(value)` or `Maybe.None()`. `Monad`, `Traversable`, `Alternative`, `MonadPlus`.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun lookup(key: String): Maybe<Int> = mapOf("a" to 1)[key]?.let { Maybe.Just(it) } ?: Maybe.None()

check((lookup("a") fmap { it + 1 }).getOrElse(0) == 2)
check(lookup("b").getOrElse(0) == 0)
check((lookup("b") orElse lookup("a")) == Maybe.Just(1))
```

`Maybe` (like `Either` and `Try`) has value-based `equals` but no `toString`/`hashCode` override, so it prints as an
object reference.

### Either

`Either.Left(error)` or `Either.Right(value)`, right-biased. `Monad`, `Traversable`, `MonadError<L>`, `Bifunctor`.
Type arguments often need to be given explicitly because Kotlin cannot infer the unused side.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either

fun divide(a: Int, b: Int): Either<String, Int> = if (b == 0) Either.Left("division by zero") else Either.Right(a / b)

check((divide(10, 2) bind { divide(it, 5) }) == Either.Right<String, Int>(1))
check((divide(10, 0) bind { divide(it, 5) }) == Either.Left<String, Int>("division by zero"))
check((divide(1, 0) handleError { -1 }) == Either.Right<String, Int>(-1))
```

### Try

`Try.Success(value)` or `Try.Failure(throwable)`. `Try.of { ... }` catches `Exception`s. `Monad`, `Traversable`,
`MonadError<Throwable>`. Exceptions thrown inside `Try.fx` also become `Failure`.

```kotlin
import com.github.kotlinz.kotlinz.data.trying.Try

val parsed = Try.of { "42".toInt() } fmap { it + 1 }
val failed = Try.of { "x".toInt() }

check(parsed == Try.Success(43))
check(failed is Try.Failure && failed.error is NumberFormatException)
check((failed handleError { -1 }) == Try.Success(-1))
check(Try.fx<Int> { Try.pure(1).bind() / 0 } is Try.Failure)
```

### Validated

`Validated.Valid(value)` or `Validated.Invalid(error)`. An `Applicative` (not a `Monad`) that **accumulates**
errors with a `Semigroup<E>`, passed as a `Class<E>`. Convert from / to `Either` with `toValidated()` /
`toEither()`.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.toValidated

data class User(val name: String, val age: Int)

val errors = ListMonoid::class.java
fun name(s: String): Validated<ListMonoid, String> =
  if (s.isNotBlank()) Validated.valid(s) else Validated.invalid(ListMonoid(listOf("name is blank")))
fun age(n: Int): Validated<ListMonoid, Int> =
  if (n >= 0) Validated.valid(n) else Validated.invalid(ListMonoid(listOf("age is negative")))

val user = Validated.liftA2(errors, ::User)

check(user(name("ann"), age(30)) == Validated.valid<ListMonoid, User>(User("ann", 30)))
check(user(name(""), age(-1)) == Validated.invalid<ListMonoid, User>(ListMonoid(listOf("name is blank", "age is negative"))))
check(Either.Left<String, Int>("e").toValidated() == Validated.invalid<String, Int>("e"))
```

`ListMonoid` stores a `List<*>`, so reading the errors back as `List<String>` needs a cast.

### Ior

Inclusive-or: `Ior.Left(a)`, `Ior.Right(b)` or `Ior.Both(a, b)`. Useful for "succeeded with warnings". Its
`Monad` accumulates the left side with a `Semigroup`, so `ap` / `bind` are extension functions that take the
semigroup class.

```kotlin
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.ior.bind
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid

val warnings = ListMonoid::class.java
fun step(n: Int): Ior<ListMonoid, Int> =
  if (n > 10) Ior.both(ListMonoid(listOf("$n is large")), n) else Ior.right(n)

check(step(20).bind(warnings) { step(it * 2) } == Ior.both(ListMonoid(listOf("20 is large", "40 is large")), 40))
check(Ior.left<ListMonoid, Int>(ListMonoid(listOf("fatal"))).bind(warnings) { step(it) } == Ior.left<ListMonoid, Int>(ListMonoid(listOf("fatal"))))
```

## Laziness and effects

### Eval

A memoized lazy value. `Eval.now(v)` is already evaluated, `Eval.later { ... }` runs the thunk once on first access
of `value`. `Monad`, `Comonad`, `MonadRec`. Note that `bind` itself is not trampolined, so very deep `bind` chains
can still overflow the stack; use `MonadRec.tailRecM` (or `Free` interpreted into `Eval`) for unbounded loops.

```kotlin
import com.github.kotlinz.kotlinz.data.eval.Eval

var evaluations = 0
val lazyValue = Eval.later { evaluations += 1; 21 } fmap { it * 2 }
val beforeAccess = evaluations

check(beforeAccess == 0)
check(lazyValue.value == 42)
check(lazyValue.value == 42)
check(evaluations == 1)
```

### IO

A suspended side effect. Nothing runs until `unsafeRun()`, and running twice runs the effect twice.
`Monad`, `MonadError<Throwable>`. Like `Eval`, `bind` is not trampolined.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.io.IO

val log = mutableListOf<String>()
fun say(s: String): IO<Unit> = IO { log += s }

val program = say("hello") bind { say("world") } fmap { log.size }
val beforeRun = log.toList()

check(beforeRun.isEmpty())
check(program.unsafeRun() == 2)
check(log == listOf("hello", "world"))
check(IO.raiseError<Int>(IllegalStateException("x")).attempt().unsafeRun() is Either.Left)
```

### Function0

A wrapped `() -> A` that is re-evaluated every time it is invoked (no memoization, unlike `Eval`). `Monad`,
`Comonad`.

```kotlin
import com.github.kotlinz.kotlinz.data.function.Function0

var counter = 0
val next = Function0 { counter += 1; counter }
val doubled = next fmap { it * 2 }

check(doubled() == 2)
check(doubled() == 4)
check(next.extract() == 3)
```

## Environment, output and state

These types are covered in detail, together with their type classes, in
[Errors and MTL-style classes](type-classes/mtl.md).

### Reader

A function `(R) -> A` that reads a shared environment. `Monad`, `MonadReader<R>`, `Distributive`,
`Representable<R>`. A top-level `ask<R>()` is also available.

```kotlin
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ask

val port: Reader<Map<String, String>, Int> = ask<Map<String, String>>() fmap { it.getValue("port").toInt() }
check(port.run(mapOf("port" to "8080")) == 8080)
```

### Writer

A value paired with accumulated output `W`, which must be a `Monoid` class. `Monad`, `MonadWriter<W>`,
`Bifunctor`.

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.writer.Writer

val cost = SumMonoid::class.java
val priced = Writer.writer(cost, Pair("a", SumMonoid(3))) bind { a -> Writer.writer(cost, Pair(a + "b", SumMonoid(4))) }
check(priced.run == Pair("ab", SumMonoid(7)))
```

### State

A function `(S) -> Pair<A, S>`. `Monad`, `MonadState<S>`, `MonadRec`. Top-level `get<S>()` and `put(s)` are also
available.

```kotlin
import com.github.kotlinz.kotlinz.data.state.State

fun push(x: Int) = State.modify<List<Int>> { listOf(x) + it }
val pop: State<List<Int>, Int> = State.state { stack -> Pair(stack.first(), stack.drop(1)) }

val program = push(1) bind { push(2) } bind { pop }
check(program.run(emptyList()) == Pair(2, listOf(1)))
```

### RWS

Reader + Writer + State in one type: a function `(R, S) -> Triple<A, S, W>`. See
[the MTL guide](type-classes/mtl.md#rws-all-three-at-once) and [fx](fx.md).

### Cont

Continuation-passing style: `Cont<R, A>` wraps `((A) -> R) -> R`. `Monad`, plus top-level `callCC` for early exit
and `eval()` for `Cont<A, A>`. `Cont` has no `fx` helper on its companion.

```kotlin
import com.github.kotlinz.kotlinz.data.cont.Cont
import com.github.kotlinz.kotlinz.data.cont.callCC
import com.github.kotlinz.kotlinz.data.cont.eval

fun safeDivide(a: Int, b: Int): Cont<String, Int> = callCC { exit ->
  if (b == 0) exit(0) bind { Cont.pure<String, Int>(it) } else Cont.pure(a / b)
}

check((Cont.pure<Int, Int>(20) bind { Cont.pure(it + 1) } fmap { it * 2 }).eval() == 42)
check(safeDivide(10, 2).run { "result $it" } == "result 5")
check(safeDivide(10, 0).run { "result $it" } == "result 0")
```

## Collections and products

### ListK

A wrapper around `List<A>` (`ListK(list)`, `listKOf(...)`, `list.toListK()`; the list is in `.value`). `Monad`,
`Traversable`, `Alternative`, `MonadPlus`, `MonadZip`. Its `fx` uses `fxReplay` (see [fx](fx.md)).

```kotlin
import com.github.kotlinz.kotlinz.data.listk.listKOf

check((listKOf(1, 2, 3) bind { listKOf(it, -it) }) == listKOf(1, -1, 2, -2, 3, -3))
check((listKOf(1, 2, 3) mzip listKOf("a", "b", "c")).value.last() == Pair(3, "c"))
check(listKOf(3, 1, 2).value.sorted() == listOf(1, 2, 3))
```

### NonEmptyList

A list with at least one element (`NonEmptyList(head, tail)`, `nonEmptyListOf(head, ...)`,
`NonEmptyList.fromList(list)` returning `null` for an empty list). `Monad`, `Traversable`, `Comonad`. It also has
`plus`, but no `Semigroup` instance class.

```kotlin
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf

val nel = nonEmptyListOf(1, 2) + nonEmptyListOf(3)
check(nel.toList() == listOf(1, 2, 3))
check(nel.head == 1)
check(NonEmptyList.fromList(emptyList<Int>()) == null)
check((nel fmap { it * 10 }) == nonEmptyListOf(10, 20, 30))
```

### Tuple2

A pair `Tuple2(first, second)`. It is a `Comonad` and `Bifunctor`, and a `Monad` in `second` when `first` is a
monoid (the "writer" monad). As with `Ior`, `ap` / `bind` are extension functions taking the monoid class. Convert
with `toPair()` / `Pair.toTuple2()`.

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.bind

val type = StringMonoid::class.java
val result = Tuple2(StringMonoid("a"), 1).bind(type) { Tuple2(StringMonoid("b"), it + 1) }
check(result == Tuple2(StringMonoid("ab"), 2))
check(result.toPair() == Pair(StringMonoid("ab"), 2))
```

### Twin

Two values of the same type, `Twin(first, second)`. `Functor` and `Representable<Boolean>`; see
[Distributive and Representable](type-classes/distributive-representable.md).

```kotlin
import com.github.kotlinz.kotlinz.data.twin.Twin

check((Twin(1, 2) fmap { it * 10 }) == Twin(10, 20))
check(Twin(1, 2).swap() == Twin(2, 1))
```

### Const

`Const<C, A>` holds a `C` and ignores `A` (a phantom type). It is a `Functor`, a `Contravariant`, and an
`Applicative` when `C` is a monoid, in which case `ap` combines the constants. This makes `traverse` with `Const`
a `foldMap`, and it powers `FreeAp.analyze`.

```kotlin
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.const.ap
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid

val type = SumMonoid::class.java
val counted = Const<SumMonoid, Int>(SumMonoid(2)).ap(type, Const<SumMonoid, (Int) -> String>(SumMonoid(3)))
check(counted == Const<SumMonoid, String>(SumMonoid(5)))
check((Const<String, Int>("kept") fmap { it + 1 }).value == "kept")
```

## Functions

### Function1

A wrapped `(A) -> B`, invokable with `f(a)`. It is the reader `Monad` in its output, a `Profunctor`, and a
`Category` / `Arrow` (encoded with a phantom `Unit`, see [Higher-kinded types](hkt.md)). See
[Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md) for the arrow combinators.

```kotlin
import com.github.kotlinz.kotlinz.data.function.Function1

val plusEnv = Function1 { env: Int -> env + 1 } bind { a -> Function1 { env: Int -> a * env } }
check(plusEnv(3) == 12)
```

### Kleisli

`Kleisli<F, A, B>` wraps `(A) -> K1<F, B>` plus a `Monad<F>`. See
[Kleisli arrows](type-classes/bifunctor-profunctor-arrow.md#kleisli-arrows).

### Predicate, Comparison, Equivalence, Op

Contravariant wrappers around `(A) -> Boolean`, `(A, A) -> Int`, `(A, A) -> Boolean` and `(A) -> R`. See
[Contravariant functors](type-classes/contravariant.md). `Comparison` converts to and from `java.util.Comparator`
with `toComparator()` / `Comparison.fromComparator(...)`.

## Functor combinators

`Compose<F, G, A>` (`F<G<A>>`), `Product<F, G, A>` (`F<A>` and `G<A>` side by side) and `Coproduct<F, G, A>`
(`F<A>` or `G<A>`) build new functors from existing ones. Their instances are created from the component
instances through companion factories.

| Combinator | Companion factories |
|---|---|
| `Compose` | `functor`, `applicative`, `applicativeOps`, `foldable`, `pure` |
| `Product` | `functor`, `applicative`, `applicativeOps`, `foldable`, `pure` |
| `Coproduct` | `functor`, `foldable`, `left`, `right`; member `fold` |

```kotlin
import com.github.kotlinz.kotlinz.data.compose.Compose
import com.github.kotlinz.kotlinz.data.coproduct.Coproduct
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.product.Product

val listOfMaybes = Compose(listKOf(Maybe.Just(1), Maybe.None(), Maybe.Just(3)))
val composeFunctor = Compose.functor(object : ListKFunctor {}, object : MaybeFunctor {})
val incremented = Compose.narrow(composeFunctor.fmap({ x: Int -> x + 1 }, listOfMaybes))
check(incremented.value == listKOf(Maybe.Just(2), Maybe.None(), Maybe.Just(4)))

val product = Product.pure(object : MaybeMonad {}, object : ListKMonad {}, 7)
check(product.first == Maybe.Just(7) && product.second == listKOf(7))

val either = Coproduct.left<Maybe.T, ListK.T, Int>(Maybe.Just(1))
check(either.fold({ "maybe" }, { "list" }) == "maybe")
```

# Monad transformers

A monad transformer adds one capability (optionality, errors, an environment, state, output) on top of an
arbitrary inner monad `F`. Kotlinz has no implicit instances, so every transformer value carries the inner
`Monad<F>` instance (and `WriterT` also the output monoid class).

| Transformer | Wraps | Adds | Type classes |
|---|---|---|---|
| `MaybeT<F, A>` | `K1<F, Maybe<A>>` | short-circuiting absence | `Functor`, `Applicative`, `Monad` |
| `EitherT<F, E, A>` | `K1<F, Either<E, A>>` | typed errors | `Functor`, `Applicative`, `Monad`, `MonadError<E>` |
| `ReaderT<F, R, A>` | `(R) -> K1<F, A>` | read-only environment | `Functor`, `Applicative`, `Monad`, `MonadReader<R>` |
| `StateT<F, S, A>` | `(S) -> K1<F, Pair<A, S>>` | state | `Functor`, `Applicative`, `Monad`, `MonadState<S>` |
| `WriterT<F, W, A>` | `K1<F, Pair<A, W>>` | accumulated output (`W` is a `Monoid` class) | `Functor`, `Applicative`, `Monad`, `MonadWriter<W>` |

Every transformer has, on its companion:

- `pure(monad, v)`, `join(monad, v)` and `fx(monad) { ... }`. `fx` is the one-shot comprehension, so it is only
  correct when the inner monad calls its continuation at most once (not for `ListK` / `NonEmptyList`; see
  [fx](fx.md#multi-shot-monads-fxreplay));
- `lift(monad, fa)` to embed an inner computation;
- capability constructors: `MaybeT.none` / `fromMaybe`, `EitherT.raiseError` / `fromEither`, `ReaderT.ask` /
  `reader`, `StateT.get` / `put` / `modify` / `gets` / `state`, `WriterT.tell` / `writer`.

`WriterT` additionally takes the monoid class: `WriterT.pure(monad, type, v)`. The underlying value is in `run`.

## MaybeT

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybet.MaybeT

val io = object : IOMonad {}
val users = mapOf(1 to "ann")

fun findUser(id: Int): MaybeT<IO.T, String> = MaybeT(io, IO { users[id]?.let { Maybe.Just(it) } ?: Maybe.None() })

val greeting = findUser(1) bind { name -> MaybeT.lift(io, IO { "hello, $name" }) }
check(IO.narrow(greeting.run).unsafeRun() == Maybe.Just("hello, ann"))
check(IO.narrow(findUser(2).getOrElse("nobody")).unsafeRun() == "nobody")
```

## EitherT

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.eithert.EitherT
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad

val io = object : IOMonad {}

fun parse(s: String): EitherT<IO.T, String, Int> =
  EitherT.fromEither(io, s.toIntOrNull()?.let { Either.Right<String, Int>(it) } ?: Either.Left("not a number: $s"))

val sum = EitherT.fx<IO.T, String, Int>(io) { parse("1").bind() + parse("2").bind() }
val failed = parse("x") bind { a -> parse("2") fmap { b -> a + b } }
val recovered = failed handleErrorWith { EitherT.pure(io, 0) }

check(IO.narrow(sum.run).unsafeRun() == Either.Right<String, Int>(3))
check(IO.narrow(failed.run).unsafeRun() == Either.Left<String, Int>("not a number: x"))
check(IO.narrow(recovered.run).unsafeRun() == Either.Right<String, Int>(0))
```

## ReaderT

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.readert.ReaderT

val maybe = object : MaybeMonad {}
fun setting(key: String): ReaderT<Maybe.T, Map<String, String>, String> =
  ReaderT(maybe) { env -> env[key]?.let { Maybe.Just(it) } ?: Maybe.None() }

val url = setting("host") bind { host -> setting("port") fmap { port -> "$host:$port" } }

check(url.run(mapOf("host" to "localhost", "port" to "80")) == Maybe.Just("localhost:80"))
check(url.run(mapOf("host" to "localhost")) == Maybe.None<String>())
check(url.local { it + ("port" to "8080") }.run(mapOf("host" to "h")) == Maybe.Just("h:8080"))
```

## StateT

`StateT` over `ListK` gives a stateful nondeterministic computation; over `Maybe` it gives state that can fail.

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.statet.StateT

val list = object : ListKMonad {}

val pick: StateT<ListK.T, Int, Int> = StateT(list) { total -> listKOf(Pair(1, total + 1), Pair(2, total + 2)) }
val twoPicks = pick bind { a -> pick fmap { b -> a * 10 + b } }

check(twoPicks.run(0) == listKOf(Pair(11, 2), Pair(12, 3), Pair(21, 3), Pair(22, 4)))
check(twoPicks.eval(0) == listKOf(11, 12, 21, 22))
check(StateT.modify<ListK.T, Int>(list) { it + 1 }.exec(1) == listKOf(2))
```

## WriterT

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.writert.WriterT

val maybe = object : MaybeMonad {}
val log = ListMonoid::class.java

fun half(n: Int): WriterT<Maybe.T, ListMonoid, Int> =
  if (n % 2 == 0) WriterT.writer(maybe, log, Pair(n / 2, ListMonoid(listOf("halved $n"))))
  else WriterT.lift(maybe, log, Maybe.None())

check((half(8) bind ::half).run == Maybe.Just(Pair(2, ListMonoid(listOf("halved 8", "halved 4")))))
check((half(6) bind ::half).run == Maybe.None<Pair<Int, ListMonoid>>())
```

## Limitations

- There is no `MonadTrans` type class; `lift` is a per-transformer companion function.
- Capabilities are not lifted through other transformers: `StateT<ReaderT<...>, ...>` is a `MonadState` but not a
  `MonadReader`. Use `lift` to reach the inner layer explicitly.
- `MaybeT` has no `Alternative` / `MonadPlus` instance.
- Stack safety depends on the inner monad's `bind`; transformers themselves are not trampolined.

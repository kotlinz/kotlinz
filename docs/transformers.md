# Monad transformers

A monad transformer adds one capability (optionality, errors, an environment, state, output, backtracking, ...) on
top of an arbitrary inner monad `F`. Kotlinz has no implicit instances, so every transformer value carries the inner
`Monad<F>` instance (and transformers with a log or warnings also the `Monoid` / `Semigroup` class).

| Transformer | Wraps | Adds | Own capability classes |
|---|---|---|---|
| `MaybeT<F, A>` | `K1<F, Maybe<A>>` | short-circuiting absence | |
| `EitherT<F, E, A>` | `K1<F, Either<E, A>>` | typed errors | `MonadError<E>` |
| `IorT<F, A, B>` | `K1<F, Ior<A, B>>` | warnings accumulated with a `Semigroup` next to a result | `MonadError<A>` |
| `ReaderT<F, R, A>` | `(R) -> K1<F, A>` | read-only environment | `MonadReader<R>` |
| `StateT<F, S, A>` | `(S) -> K1<F, Pair<A, S>>` | state | `MonadState<S>` |
| `WriterT<F, W, A>` | `K1<F, Pair<A, W>>` | accumulated output (`W` is a `Monoid` class) | `MonadWriter<W>` |
| `AccumT<F, W, A>` | `(W) -> K1<F, Pair<A, W>>` | append-only state that can be read back | `look` / `add` |
| `RWST<F, R, W, S, A>` | `(R, S) -> K1<F, Triple<A, S, W>>` | environment, log and state at once | `MonadReader`, `MonadWriter`, `MonadState` |
| `IdentityT<F, A>` | `K1<F, A>` | nothing (a neutral layer) | |
| `ContT<R, F, A>` | `((A) -> K1<F, R>) -> K1<F, R>` | continuations: early exit, `shift` / `reset` | `MonadCont` |
| `ListT<F, A>` | `K1<F, Maybe<Pair<A, ListT<F, A>>>>` | an effectful lazy stream ("ListT done right") | `Alternative`, `MonadPlus` |
| `LogicT<F, A>` | Church-encoded search | backtracking with fair choice and pruning | `MonadPlus`, `MonadLogic` |
| `FreeT<F, M, A>` | `K1<M, FreeF<F, A, FreeT<F, M, A>>>` | instructions of a functor `F` interleaved with `M` effects | |
| `SelectT<R, F, A>` | `((A) -> K1<F, R>) -> K1<F, A>` | selection: choose knowing how the rest scores the choice | |
| `IndexedStateT<F, I, O, A>` | `(I) -> K1<F, Pair<A, O>>` | state whose type changes from `I` to `O` | `IxMonad` |

Every transformer except `IndexedStateT` is a `Functor`, `Applicative` and `Monad`, and a `MonadTrans` over its base
(see [below](#monadtrans-mfunctor-and-monadio)). `LogicT` is described in [Effects](effects.md#logic-and-logict).

Every transformer has, on its companion:

- `pure(monad, v)`, `join(monad, v)` and `fx(monad) { ... }`. For most transformers `fx` is the one-shot
  comprehension, so it is only correct when the inner monad calls its continuation at most once (not for `ListK` /
  `NonEmptyList`; see [fx](fx.md#multi-shot-monads-fxreplay)). `ListT`, `LogicT`, `FreeT` and `SelectT` use
  `fxReplay` because they are multi-shot themselves;
- `lift(monad, fa)` to embed an inner computation, and `liftIO(monadIO, io)` when the base is a `MonadIO`;
- capability constructors: `MaybeT.none` / `fromMaybe`, `EitherT.raiseError` / `fromEither`, `IorT.left` / `right`
  / `both` / `fromIor`, `ReaderT.ask` / `reader`, `StateT.get` / `put` / `modify` / `gets` / `state`,
  `WriterT.tell` / `writer`, `AccumT.look` / `looks` / `add` / `accum`, `RWST.ask` / `tell` / `get` / `put` / ...,
  `ContT.callCC` / `shift` / `reset`, `ListT.empty` / `cons` / `fromList` / `defer`.

`WriterT`, `AccumT`, `RWST` and `IorT` additionally take the monoid or semigroup class:
`WriterT.pure(monad, type, v)`. The underlying value is in `run`.

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

## IorT

`IorT` keeps going on `Ior.Both` (accumulating the left side with its semigroup) and stops on a pure `Ior.Left`:

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.iort.IorT
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid

val identity = object : IdentityMonad {}
val type = StringMonoid::class.java

val warned = IorT.both<Identity.T, StringMonoid, Int>(identity, type, StringMonoid("warn;"), 1) bind { x ->
  IorT.both(identity, type, StringMonoid("again;"), x + 1)
}
val stopped = warned bind { IorT.left<Identity.T, StringMonoid, Int>(identity, type, StringMonoid("fatal")) }

check(Identity.narrow(warned.run).value == Ior.Both(StringMonoid("warn;again;"), 2))
check(Identity.narrow(stopped.run).value == Ior.Left<StringMonoid, Int>(StringMonoid("warn;again;fatal")))
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

## AccumT

`AccumT` is append-only state: `run` receives everything accumulated so far and returns only the newly added
output. Unlike `WriterT`, the output can be read back with `look`; unlike `StateT`, it can only grow.

```kotlin
import com.github.kotlinz.kotlinz.data.accumt.AccumT
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid

val identity = object : IdentityMonad {}
val type = StringMonoid::class.java

val program = AccumT.add<Identity.T, StringMonoid>(identity, type, StringMonoid("a")) bind {
  AccumT.look<Identity.T, StringMonoid>(identity, type)
}

check(Identity.narrow(program.run(StringMonoid(">"))).value == Pair(StringMonoid(">a"), StringMonoid("a")))
```

## RWST

`RWST` combines `ReaderT`, `WriterT` and `StateT` in one layer, with `eval` / `exec` helpers:

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.rwst.RWST

val identity = object : IdentityMonad {}
val type = StringMonoid::class.java

val program = RWST.fx<Identity.T, Int, StringMonoid, Int, Int>(identity, type) {
  val r = RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type).bind()
  RWST.tell<Identity.T, Int, StringMonoid, Int>(identity, type, StringMonoid("r=$r")).bind()
  RWST.modify<Identity.T, Int, StringMonoid, Int>(identity, type) { it + r }.bind()
  r * 10
}

check(Identity.narrow(program.run(2, 10)).value == Triple(20, 12, StringMonoid("r=2")))
check(Identity.narrow(program.exec(2, 10)).value == Pair(12, StringMonoid("r=2")))
```

## IdentityT

`IdentityT` adds no effect. It is useful as a neutral layer in generic transformer code, and it lifts every
capability of its base (`MonadState`, `MonadReader`, `MonadWriter`, `MonadError`, `MonadIO`).

```kotlin
import com.github.kotlinz.kotlinz.data.identityt.IdentityT
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad

val maybe = object : MaybeMonad {}
val program = IdentityT.lift(maybe, Maybe.Just(1)) bind { x -> IdentityT.pure(maybe, x + 1) }

check(program.run == Maybe.Just(2))
```

## ContT

`ContT` is `Cont` whose continuation runs in `F`. `callCC` gives an early exit, `shift` / `reset` give delimited
control, and `eval()` runs the computation with `pure` as the final continuation.

```kotlin
import com.github.kotlinz.kotlinz.data.contt.ContT
import com.github.kotlinz.kotlinz.data.contt.eval
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad

val identity = object : IdentityMonad {}

// exit(-1) skips the rest of the callCC block, including the "+ 100"
fun program(b: Int): ContT<Int, Identity.T, Int> = ContT.callCC<Int, Identity.T, Int, Int>(identity) { exit ->
  (if (b == 0) exit(-1) else ContT.pure(identity, 10 / b)) fmap { it + 100 }
}

val shifted = ContT.reset<Int, Int, Identity.T>(identity,
  ContT.shift<Int, Identity.T, Int>(identity) { k -> ContT.lift(identity, identity.fmap({ it * 2 }, k(10))) } fmap { it + 1 })

check(Identity.narrow(program(2).eval()).value == 105)
check(Identity.narrow(program(0).eval()).value == -1)
check(Identity.narrow(shifted.eval()).value == 22)
```

## ListT

`ListT` is "ListT done right": every step is an effect that yields the next element and the rest of the stream, so
effects are interleaved with elements, streams are lazy (and can be infinite with `defer`), and the monad laws hold
for every base monad. `take(n)` and `toList()` consume it.

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.listt.ListT

val io = object : IOMonad {}
val seen = mutableListOf<Int>()

fun from(n: Int): ListT<IO.T, Int> = ListT.cons(io, n, ListT.defer(io) { from(n + 1) })

val squares = from(1) bind { x -> ListT.lift(io, IO { seen += x; x * x }) }

check(IO.narrow(squares.take(3).toList()).unsafeRun() == listOf(1, 4, 9))
check(seen == listOf(1, 2, 3))
```

## FreeT

`FreeT<F, M, A>` is the free monad transformer: instructions of a functor `F` (unlike `Free`, `F` must be a
`Functor`) interleaved with effects of `M`. `liftF` embeds an instruction, `lift` an `M` effect, and `iterT` /
`foldMap` interpret the program into `M`. `hoistFreeT` swaps `M` and `transFreeT` swaps `F`.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.freet.FreeT
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonad
import com.github.kotlinz.kotlinz.type.Functor

data class Say<A>(val msg: String, val next: A) : K1<Say.T, A> {
  class T
  companion object {
    fun <A> narrow(v: K1<T, A>): Say<A> = v as Say<A>
  }
}

object SayFunctor : Functor<Say.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Say.T, A>): Say<B> = Say.narrow(v).let { Say(it.msg, f(it.next)) }
}

val state = object : StateMonad<List<String>> {}
fun say(msg: String): FreeT<Say.T, K1<State.T, List<String>>, Unit> = FreeT.liftF(SayFunctor, state, Say(msg, Unit))

val program = say("hello") bind { FreeT.lift(SayFunctor, state, State.gets { log: List<String> -> log.size }) } bind { n ->
  say("seen $n") fmap { n }
}
val interpreted = program.iterT { layer ->
  val s = Say.narrow(layer)
  State.modify<List<String>> { it + s.msg } bind { State.narrow(s.next) }
}

check(State.narrow(interpreted).run(emptyList()) == Pair(1, listOf("hello", "seen 1")))
```

## SelectT and Select

`Select<R, A>` is `((A) -> R) -> A`: a choice of an `A` given a way to score each candidate. Sequencing selections
makes every choice knowing how the rest of the computation scores it, which solves greedy optimisation and
game-playing problems. `SelectT<R, F, A>` runs the scoring in a monad `F`; `Select.toSelectT()` converts.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.select.Select
import com.github.kotlinz.kotlinz.data.selectt.SelectT

fun argmax(xs: List<Int>) = Select<Int, Int> { k -> xs.maxBy(k) }
val pair = argmax(listOf(1, 2, 3)) bind { x -> argmax(listOf(1, 2, 3)) fmap { y -> Pair(x, y) } }

check(pair.run { (x, y) -> x - y } == Pair(3, 1))

val maybe = object : MaybeMonad {}
val pick = SelectT<Int, Maybe.T, Int>(maybe) { k -> Maybe.Just(listOf(1, 2, 3).maxBy { (k(it) as Maybe.Just).value }) }
check(pick.run { Maybe.Just(-it * it) } == Maybe.Just(1))
```

## IndexedState and IndexedStateT

`IndexedState<I, O, A>` is `(I) -> Pair<A, O>`: a state computation whose state *type* changes, so protocol steps can
be tracked in the type and out-of-order steps fail to compile. It is an `IxMonad` (and a plain `Monad` via
`IndexedStateMonad` when `I == O`; `toState()` converts). `IndexedStateT<F, I, O, A>` runs each step in `F`.

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.indexedstate.IndexedState
import com.github.kotlinz.kotlinz.data.indexedstatet.IndexedStateT

class Closed
class Open(val path: String)

val open = IndexedState.modify<Closed, Open> { Open("a.txt") }
val read = IndexedState.gets<Open, String> { "contents of ${it.path}" }
val close = IndexedState.modify<Open, Closed> { Closed() }

val session = open bind { read } bind { text -> close fmap { text } }
check(session.eval(Closed()) == "contents of a.txt")

val identity = object : IdentityMonad {}
val program = IndexedStateT.get<Identity.T, Int>(identity) bind { n -> IndexedStateT.put<Identity.T, Int, String>(identity, "n=$n") }
check(Identity.narrow(program.run(1)).value == Pair(Unit, "n=1"))
```

## MonadTrans, MFunctor and MonadIO

Three type classes describe transformers generically. Kotlin's encoding cannot abstract over "a transformer applied
to any base", so each instance fixes the base and names the transformed monad explicitly:

<!-- nocheck -->
```kotlin
interface MonadTrans<F, T>: Monad<T> { fun <A> lift(v: K1<F, A>): K1<T, A> }
interface MFunctor<F, G, TF, TG> { fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<TF, A>): K1<TG, A> }
interface MonadIO<T>: Monad<T> { fun <A> liftIO(v: IO<A>): K1<T, A> }
```

- `MonadTrans` (`<Transformer>MonadTrans`, every transformer except `IndexedStateT`): `lift` embeds a base
  computation. The companion `lift(monad, fa)` uses it.
- `MFunctor` (`<Transformer>MFunctor` for `MaybeT`, `EitherT`, `IorT`, `ReaderT`, `StateT`, `WriterT`, `AccumT`,
  `RWST`, `IdentityT`, `FreeT`): `hoist` swaps the base monad through a natural transformation. The member
  `hoist(targetMonad, nt)` (`hoistFreeT` on `FreeT`) uses it.
- `MonadIO` (`<Transformer>MonadIO`, every transformer except `FreeT`, `SelectT` and `IndexedStateT`): `liftIO`
  embeds an `IO` through the whole stack when the base is a `MonadIO` (`IOMonadIO` at the bottom).

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.readert.ReaderT
import com.github.kotlinz.kotlinz.data.readert.ReaderTMonadIO
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// liftIO through StateT<ReaderT<IO>>
val io = object : IOMonadIO {}
val readerT = object : ReaderTMonadIO<IO.T, String> { override val monad = io }
var effects = 0
val step = StateT.liftIO<K2<ReaderT.T, IO.T, String>, Int, Int>(readerT, IO { effects += 1; 42 })
check(IO.narrow(ReaderT.narrow(step.run(0)).run("env")).unsafeRun() == Pair(42, 0))
check(effects == 1)

// hoist: move a StateT from Identity to IO
val identity = object : IdentityMonad {}
val counter = StateT.modify<Identity.T, Int>(identity) { it + 1 }
val identityToIO = object : NaturalTransformation<Identity.T, IO.T> {
  override fun <A> invoke(fa: K1<Identity.T, A>): K1<IO.T, A> = IO.pure(Identity.narrow(fa).value)
}
check(IO.narrow(counter.hoist(io, identityToIO).exec(1)).unsafeRun() == 2)
```

## Lifted MTL instances

Capability classes are lifted through the five classic transformers (following Haskell's `mtl`): each lifted
instance narrows `monad` to the base capability (for example `override val monad: MonadReader<F, R>`) and extends the
transformer's `MonadTrans` instance. There are no companion helpers for them; instantiate the interface directly.

| Transformer | `MonadState` | `MonadReader` | `MonadWriter` | `MonadError` |
|---|---|---|---|---|
| `MaybeT` | `MaybeTMonadState` | `MaybeTMonadReader` | `MaybeTMonadWriter` | `MaybeTMonadError` |
| `EitherT` | `EitherTMonadState` | `EitherTMonadReader` | `EitherTMonadWriter` | native (`MonadError<E>`) |
| `ReaderT` | `ReaderTMonadState` | native | `ReaderTMonadWriter` | `ReaderTMonadError` |
| `StateT` | native | `StateTMonadReader` | `StateTMonadWriter` | `StateTMonadError` |
| `WriterT` | `WriterTMonadState` | `WriterTMonadReader` | native | `WriterTMonadError` |
| `IdentityT` | `IdentityTMonadState` | `IdentityTMonadReader` | `IdentityTMonadWriter` | `IdentityTMonadError` |

`local`, `listen`, `pass` and `handleErrorWith` go through the base operation, so for example recovering inside
`StateT` restarts from the input state. `IorT`, `AccumT`, `RWST`, `ContT`, `ListT`, `LogicT`, `FreeT` and
`SelectT` do not lift these capabilities; use `lift` to reach the inner layer.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.readert.ReaderT
import com.github.kotlinz.kotlinz.data.readert.ReaderTMonadReader
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.data.statet.StateTMonadReader

typealias Env = K2<ReaderT.T, Identity.T, String>

val reader = object : ReaderTMonadReader<Identity.T, String> { override val monad = object : IdentityMonad {} }
val stateOverReader = object : StateTMonadReader<Env, Int, String> { override val monad = reader }

val program = StateT.narrow(stateOverReader.ask()) bind { env ->
  StateT.modify<Env, Int>(reader) { it + env.length } fmap { env.uppercase() }
}
val shadowed = StateT.narrow(stateOverReader.local({ "much longer" }, program))

check(Identity.narrow(ReaderT.narrow(program.run(0)).run("env")).value == Pair("ENV", 3))
check(Identity.narrow(ReaderT.narrow(shadowed.run(0)).run("env")).value == Pair("MUCH LONGER", 11))
```

## Limitations

- `MonadTrans` / `MFunctor` instances fix the base monad, so generic code over "any transformer" is not possible;
  each stack is spelled out.
- Capabilities are lifted only through `MaybeT`, `EitherT`, `ReaderT`, `StateT`, `WriterT` and `IdentityT` (see
  [above](#lifted-mtl-instances)); other transformers need an explicit `lift`.
- `MaybeT` has no `Alternative` / `MonadPlus` instance.
- Stack safety depends on the inner monad's `bind`; transformers themselves are not trampolined, and `ContT`,
  `ListT`, `LogicT` and `FreeT` can overflow on very deep programs.

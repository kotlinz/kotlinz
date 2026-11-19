# Errors and MTL-style classes

These classes describe *capabilities* of a monad independently of the concrete type, in the style of Haskell's
`mtl`: failing with an error, reading an environment, threading state, and accumulating output.

| Type class | Capability | Native instances |
|---|---|---|
| `ApplicativeError<T, E>` / `MonadError<T, E>` | raise and recover from errors of type `E` | `Either<E, _>`, `Try` (`Throwable`), `IO` (`Throwable`), `EitherT<F, E, _>`, `IorT<F, A, _>` |
| `MonadReader<T, R>` | read an environment `R` | `Reader<R, _>`, `ReaderT<F, R, _>`, `RWS`, `RWST` |
| `MonadState<T, S>` | read and write a state `S` | `State<S, _>`, `StateT<F, S, _>`, `RWS`, `RWST` |
| `MonadWriter<T, W>` | append to an output `W` | `Writer<W, _>`, `WriterT<F, W, _>`, `RWS`, `RWST` |
| `MonadTrans<F, T>` / `MFunctor` | lift a base computation / swap the base monad | every transformer (see [below](#monadtrans-and-mfunctor)) |
| `MonadIO<T>` | run an `IO` action | `IO` and most transformers over a `MonadIO` base |
| `MonadCont<T>` | capture the current continuation (`callCC`) | `Cont<R, _>`, `ContT<R, F, _>` |
| `MonadLogic<T>` | split, interleave and prune backtracking searches | `LogicT<F, _>` (and `Logic`) |

Transformers also *lift* the classes of their base monad, as in Haskell's `mtl`; see
[Lifting through transformers](#lifting-through-transformers). `MonadFix` is covered with the core classes in
[Functor, Applicative, Monad](functor-applicative-monad.md#monadfix).

## ApplicativeError and MonadError

<!-- nocheck -->
```kotlin
interface ApplicativeError<T, E>: Applicative<T> {
  fun <A> raiseError(e: E): K1<T, A>
  fun <A> handleErrorWith(f: (E) -> K1<T, A>, v: K1<T, A>): K1<T, A>
  fun <A> handleError(f: (E) -> A, v: K1<T, A>): K1<T, A>
  fun <A> attempt(v: K1<T, A>): K1<T, Either<E, A>>
  fun <A> fromEither(v: Either<E, A>): K1<T, A>
}
interface MonadError<T, E>: Monad<T>, ApplicativeError<T, E> {
  fun <A> ensure(error: (A) -> E, predicate: (A) -> Boolean, v: K1<T, A>): K1<T, A>
  fun <A> rethrow(v: K1<T, Either<E, A>>): K1<T, A>
}
```

`raiseError` is on the companions; `handleErrorWith`, `handleError` and (for `Try` / `IO`) `attempt` are members.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.EitherMonadError
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.trying.Try

val failed = Either.raiseError<String, Int>("boom")
check((failed handleError { it.length }) == Either.Right<String, Int>(4))
check((failed handleErrorWith { Either.Left("still $it") }) == Either.Left<String, Int>("still boom"))

val division = Try.of { 10 / 0 }
check(division is Try.Failure)
check(division.getOrElse(-1) == -1)
check(division.attempt().getOrElse(Either.Right(0)) is Either.Left)

val io = IO<Int> { throw IllegalStateException("io failed") } handleError { e -> e.message!!.length }
check(io.unsafeRun() == 9)

val eitherError = object : EitherMonadError<String> {}
val positive = Either.narrow(eitherError.ensure({ n: Int -> "$n is not positive" }, { it > 0 }, Either.Right(-1)))
check(positive == Either.Left<String, Int>("-1 is not positive"))
```

## MonadReader

<!-- nocheck -->
```kotlin
interface MonadReader<T, R>: Monad<T> {
  fun ask(): K1<T, R>
  fun <A> local(f: (R) -> R, v: K1<T, A>): K1<T, A>
  fun <A> reader(f: (R) -> A): K1<T, A>
}
```

```kotlin
import com.github.kotlinz.kotlinz.data.reader.Reader

data class Config(val name: String, val verbose: Boolean)

val greeting: Reader<Config, String> = Reader.reader { c: Config -> "hello, ${c.name}" }
val loud = Reader.ask<Config>() bind { c -> greeting fmap { if (c.verbose) it.uppercase() else it } }

check(loud.run(Config("kotlinz", verbose = false)) == "hello, kotlinz")
check(loud.local { it.copy(verbose = true) }.run(Config("kotlinz", verbose = false)) == "HELLO, KOTLINZ")
```

## MonadState

<!-- nocheck -->
```kotlin
interface MonadState<T, S>: Monad<T> {
  fun get(): K1<T, S>
  fun put(s: S): K1<T, Unit>
  fun modify(f: (S) -> S): K1<T, Unit>
  fun <A> gets(f: (S) -> A): K1<T, A>
  fun <A> state(f: (S) -> Pair<A, S>): K1<T, A>
}
```

`State` exposes the operations on its companion. `run(s)` returns `(value, finalState)`; `eval` and `exec` return
only one of them.

```kotlin
import com.github.kotlinz.kotlinz.data.state.State

val nextId: State<Int, Int> = State.get<Int>() bind { n -> State.put(n + 1) fmap { n } }
val twoIds = nextId bind { a -> nextId fmap { b -> listOf(a, b) } }

check(twoIds.run(10) == Pair(listOf(10, 11), 12))
check(twoIds.eval(0) == listOf(0, 1))
check(State.modify<Int> { it * 2 }.exec(21) == 42)
```

### Writing code against the capability

Because the capability is an interface, a function can be written once and used with any instance, for example
both `State` and the `StateT` transformer (see [Monad transformers](../transformers.md)):

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadState
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.data.statet.StateTMonadState
import com.github.kotlinz.kotlinz.type.monad.MonadState

fun <T> increment(m: MonadState<T, Int>): K1<T, Int> = m.bind({ n -> m.fmap({ _: Unit -> n }, m.put(n + 1)) }, m.get())

val viaState = State.narrow(increment(object : StateMonadState<Int> {}))
check(viaState.run(1) == Pair(1, 2))

val stateT = object : StateTMonadState<Identity.T, Int> { override val monad = object : IdentityMonad {} }
val viaStateT = StateT.narrow(increment(stateT))
check(Identity.narrow(viaStateT.run(1)) == Identity(Pair(1, 2)))
```

## MonadWriter

<!-- nocheck -->
```kotlin
interface MonadWriter<T, W>: Monad<T> {
  fun <A> writer(v: Pair<A, W>): K1<T, A>
  fun <A> listen(v: K1<T, A>): K1<T, Pair<A, W>>
  fun <A> pass(v: K1<T, Pair<A, (W) -> W>>): K1<T, A>
  fun tell(w: W): K1<T, Unit>
  fun <A, B> listens(f: (W) -> B, v: K1<T, A>): K1<T, Pair<A, B>>
  fun <A> censor(f: (W) -> W, v: K1<T, A>): K1<T, A>
}
```

`Writer` needs a `Monoid` class for its output (see [Monoids](../monoids.md)). Its `run` property is the pair
`(value, output)`.

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.writer.Writer

val logType = ListMonoid::class.java
fun log(message: String) = Writer.tell(logType, ListMonoid(listOf(message)))

val program = log("start") bind { Writer.pure(logType, 21) } bind { n -> log("doubling $n") fmap { n * 2 } }

check(program.run == Pair(42, ListMonoid(listOf("start", "doubling 21"))))
check(program.listen().run.first == Pair(42, ListMonoid(listOf("start", "doubling 21"))))
check(program.censor { ListMonoid(it.value.reversed()) }.run.second == ListMonoid(listOf("doubling 21", "start")))
```

## RWS: all three at once

`RWS<R, W, S, A>` combines `MonadReader`, `MonadWriter` and `MonadState` in one data type. Its primitives live on
the companion and take the writer's monoid class; type arguments usually have to be spelled out:

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.rws.RWS

val type = ListMonoid::class.java

val step: RWS<Int, ListMonoid, Int, Int> = RWS.ask<Int, ListMonoid, Int>(type) bind { r ->
  RWS.get<Int, ListMonoid, Int>(type) bind { s ->
    RWS.tell<Int, ListMonoid, Int>(type, ListMonoid(listOf("r=$r s=$s"))) bind {
      RWS.put<Int, ListMonoid, Int>(type, s + r) fmap { r * s }
    }
  }
}

check(step.run(2, 5) == Triple(10, 7, ListMonoid(listOf("r=2 s=5"))))
check(step.eval(2, 5) == Pair(10, ListMonoid(listOf("r=2 s=5"))))
```

The same program reads more naturally with [`RWS.fx`](../fx.md).

## MonadTrans and MFunctor

<!-- nocheck -->
```kotlin
interface MonadTrans<F, T>: Monad<T> { fun <A> lift(v: K1<F, A>): K1<T, A> }
interface MFunctor<F, G, TF, TG> { fun <A> hoist(nt: NaturalTransformation<F, G>, v: K1<TF, A>): K1<TG, A> }
```

`lift` embeds a computation of the base monad `F` into the transformed monad `T`; `hoist` changes the base monad of
a transformer with a [natural transformation](../category-theory.md). The `K1` encoding cannot express "transformer
`t` applied to an arbitrary base", so both classes name the base and the transformed monad explicitly
(`MaybeTMonadTrans<F> : MonadTrans<F, K1<MaybeT.T, F>>`). `MFunctor` instances take the monad of the target base as
`target`.

Every transformer has a `MonadTrans` instance and a companion `lift(monad, v)`. `MFunctor` is provided for `MaybeT`,
`EitherT`, `ReaderT`, `StateT`, `WriterT`, `IdentityT`, `RWST`, `IorT`, `AccumT` and `FreeT`, with a member
`hoist(target, nt)`.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybet.MaybeT
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

val lifted = MaybeT.lift(object : IdentityMonad {}, Identity(1))
check(lifted.run == Identity(Maybe.Just(1)))

val identityToList = object : NaturalTransformation<Identity.T, ListK.T> {
  override fun <A> invoke(fa: K1<Identity.T, A>): K1<ListK.T, A> = listKOf(Identity.narrow(fa).value)
}
val hoisted = lifted.hoist(object : ListKMonad {}, identityToList)
check(hoisted.run == listKOf(Maybe.Just(1)))
```

## Lifting through transformers

A transformer stack should keep the capabilities of the monads below it. The lifted instances narrow `monad` to the
base class (for example `override val monad: MonadState<F, S>`) and implement the capability through `lift`, using
the `mtl` semantics for `local`, `listen`, `pass` and `handleErrorWith` (for example, `StateT` recovery restarts from
the input state).

| Transformer | `MonadState` | `MonadReader` | `MonadWriter` | `MonadError` | `MonadIO` | Other |
|---|---|---|---|---|---|---|
| `MaybeT` | lifted | lifted | lifted | lifted | lifted | |
| `EitherT` | lifted | lifted | lifted | native (`E`) | lifted | |
| `ReaderT` | lifted | native | lifted | lifted | lifted | |
| `StateT` | native | lifted | lifted | lifted | lifted | |
| `WriterT` | lifted | lifted | native | lifted | lifted | |
| `IdentityT` | lifted | lifted | lifted | lifted | lifted | |
| `RWST` | native | native | native | | lifted | |
| `IorT` | | | | native (`Semigroup` `A`) | lifted | |
| `AccumT` | | | | | lifted | `look` / `add` |
| `ContT` | | | | | lifted | `MonadCont` |
| `ListT` | | | | | lifted | `MonadPlus` |
| `LogicT` | | | | | lifted | `MonadLogic` |
| `FreeT`, `SelectT` | | | | | | |

No companion helpers exist for the lifted instances; build them with an anonymous object. The same
`increment` function from [above](#writing-code-against-the-capability) runs inside `MaybeT` over `State`:

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybet.MaybeT
import com.github.kotlinz.kotlinz.data.maybet.MaybeTMonadState
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadState
import com.github.kotlinz.kotlinz.type.monad.MonadState

fun <T> increment(m: MonadState<T, Int>): K1<T, Int> = m.bind({ n -> m.fmap({ _: Unit -> n }, m.put(n + 1)) }, m.get())

val maybeOverState = object : MaybeTMonadState<K1<State.T, Int>, Int> { override val monad = object : StateMonadState<Int> {} }
val program = MaybeT.narrow(increment(maybeOverState))
check(State.narrow(program.run).run(1) == Pair(Maybe.Just(1), 2))
```

## MonadIO

<!-- nocheck -->
```kotlin
interface MonadIO<T>: Monad<T> { fun <A> liftIO(v: IO<A>): K1<T, A> }
```

`liftIO` runs an [`IO`](../effects.md) action anywhere in a stack whose bottom is `IO`, without counting `lift`
calls. `IOMonadIO` is the identity; each transformer instance narrows `monad` to `MonadIO<F>` and lifts once, so the
instances compose through arbitrary depth. Transformers have a companion `liftIO(monad, io)`.

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.statet.StateT

val readConfig = IO { "verbose" }
val step = StateT.liftIO<IO.T, Int, String>(object : IOMonadIO {}, readConfig)
check(IO.narrow(step.run(7)).unsafeRun() == Pair("verbose", 7))
```

## MonadCont

<!-- nocheck -->
```kotlin
interface MonadCont<T>: Monad<T> { fun <A, B> callCC(f: ((A) -> K1<T, B>) -> K1<T, A>): K1<T, A> }
```

`callCC` passes the current continuation to `f` as an "exit" function: calling it aborts the rest of the block and
returns its argument. Instances: `ContMonadCont<R>` (the top-level `callCC` for `Cont` predates the class and behaves
the same) and `ContTMonadCont<R, F>`.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.cont.Cont
import com.github.kotlinz.kotlinz.data.cont.ContMonadCont
import com.github.kotlinz.kotlinz.data.cont.eval
import com.github.kotlinz.kotlinz.type.monad.MonadCont

fun <T> safeDiv(m: MonadCont<T>, a: Int, b: Int): K1<T, Int> =
  m.callCC<Int, Int> { exit -> if (b == 0) exit(-1) else m.pure(a / b) }

val cont = object : ContMonadCont<Int> {}
check(Cont.narrow(safeDiv(cont, 10, 2)).eval() == 5)
check(Cont.narrow(safeDiv(cont, 10, 0)).eval() == -1)
```

## MonadLogic

<!-- nocheck -->
```kotlin
interface MonadLogic<T>: MonadPlus<T> {
  fun <A> msplit(v: K1<T, A>): K1<T, Maybe<Pair<A, K1<T, A>>>>
  // derived: interleave, fairBind, ifte, once, lnot
}
```

`MonadLogic` adds control over backtracking search: `interleave` and `fairBind` are fair versions of `mplus` and
`bind` (they do not starve the second branch when the first is infinite), `ifte` is a soft cut, `once` keeps only the
first solution, and `lnot` succeeds only when its argument has no solution. The instance is `LogicT<F, _>`; `Logic` is
`LogicT` over `Identity`. The members are described in [Effects](../effects.md).

```kotlin
import com.github.kotlinz.kotlinz.data.logict.Logic
import com.github.kotlinz.kotlinz.data.logict.observeAll

check((Logic.of(1, 2, 3) interleave Logic.of(10, 20)).observeAll() == listOf(1, 10, 2, 20, 3))
check(Logic.of(1, 2, 3).once().observeAll() == listOf(1))
check(Logic.of(1, 2).ifte({ n -> Logic.pure(n * 10) }, Logic.pure(0)).observeAll() == listOf(10, 20))
check(Logic.empty<Int>().lnot().observeAll() == listOf(Unit))
```

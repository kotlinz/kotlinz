# Monad comprehension: `fx` and `fxReplay`

Chains of `bind` quickly become nested lambdas. `fx` provides Haskell `do` / Scala `for` style syntax on top of
Kotlin coroutines: inside the block, `.bind()` extracts the value of a monadic computation, and the result of the
block is wrapped with `pure`.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

val sum = Maybe.fx {
  val a = parse("1").bind()
  val b = parse("2").bind()
  a + b
}
val failed = Maybe.fx { parse("1").bind() + parse("x").bind() }

check(sum == Maybe.Just(3))
check(failed == Maybe.None<Int>())
```

Both functions live in `com.github.kotlinz.kotlinz.syntax`:

<!-- nocheck -->
```kotlin
fun <T, A> Monad<T>.fx(block: suspend MonadScope<T>.() -> A): K1<T, A>
fun <T, A> Monad<T>.fxReplay(block: suspend MonadScope<T>.() -> A): K1<T, A>
```

## Where it is available

| Companion helper | Implementation | Extra arguments |
|---|---|---|
| `Identity.fx`, `Maybe.fx`, `Either.fx`, `Try.fx`, `Eval.fx`, `IO.fx`, `Function0.fx` | `fx` | |
| `Reader.fx`, `State.fx`, `Function1.fx` | `fx` | |
| `Writer.fx`, `Tuple2.fx`, `RWS.fx` | `fx` | monoid class |
| `Ior.fx` | `fx` | semigroup class |
| `MaybeT.fx`, `EitherT.fx`, `ReaderT.fx`, `StateT.fx` | `fx` | inner monad |
| `WriterT.fx` | `fx` | inner monad and monoid class |
| `ListK.fx`, `NonEmptyList.fx`, `Free.fx` | `fxReplay` | |

Any other monad can use the extension directly on an instance, for example `Cont` or `Codensity`, which have no
companion helper:

```kotlin
import com.github.kotlinz.kotlinz.data.cont.Cont
import com.github.kotlinz.kotlinz.data.cont.ContMonad
import com.github.kotlinz.kotlinz.data.cont.eval
import com.github.kotlinz.kotlinz.syntax.fx

val viaInstance = Cont.narrow(object : ContMonad<Int> {}.fx { Cont.pure<Int, Int>(20).bind() + 1 })
check(viaInstance.eval() == 21)
```

Type arguments that cannot be inferred from the block (the error type of `Either`, the environment of `Reader`,
the state of `State`, ...) have to be given explicitly:

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.rws.RWS
import com.github.kotlinz.kotlinz.data.state.State

val either = Either.fx<String, Int> {
  val a = Either.Right<String, Int>(1).bind()
  val b = Either.Left<String, Int>("boom").bind()
  a + b
}
check(either == Either.Left<String, Int>("boom"))

val counter = State.fx<Int, Int> {
  val n = State.get<Int>().bind()
  State.put(n + 1).bind()
  n * 10
}
check(counter.run(1) == Pair(10, 2))

val type = ListMonoid::class.java
val rws = RWS.fx<Int, ListMonoid, Int, Int>(type) {
  val r = RWS.ask<Int, ListMonoid, Int>(type).bind()
  val s = RWS.get<Int, ListMonoid, Int>(type).bind()
  RWS.tell<Int, ListMonoid, Int>(type, ListMonoid(listOf("r=$r s=$s"))).bind()
  RWS.put<Int, ListMonoid, Int>(type, r + s).bind()
  r * s
}
check(rws.run(2, 5) == Triple(10, 7, ListMonoid(listOf("r=2 s=5"))))
```

## Lazy monads

`fx` builds the computation inside `bind`, so each *run* of a lazy monad (`IO`, `Eval`, `State`, `Reader`, ...)
starts a fresh coroutine. Nothing is executed when the value is created, and it can be run repeatedly:

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO

var count = 0
val io = IO.fx { IO { count += 1; count }.bind() * 10 }
val before = count

check(before == 0)
check(io.unsafeRun() == 10)
check(io.unsafeRun() == 20)
```

## Multi-shot monads: `fxReplay`

A Kotlin coroutine continuation can be resumed only once, so `fx` is correct only for monads whose `bind` calls the
continuation **at most once** per run. `ListK` and `NonEmptyList` call it once per element, and `Free` may be
interpreted into such a monad. For these, `fxReplay` re-runs the block from the start for every bound value,
replaying previously bound values from a history:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf

val pairs = ListK.fx {
  val a = listKOf(1, 2).bind()
  val b = listKOf("x", "y").bind()
  "$a$b"
}
check(pairs == listKOf("1x", "1y", "2x", "2y"))
```

Code *between* binds therefore runs more than once, so keep side effects out of `fxReplay` blocks:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf

var executions = 0
val result = ListK.fx {
  executions += 1
  val a = listKOf(1, 2, 3).bind()
  a * 2
}

check(result == listKOf(2, 4, 6))
check(executions == 4)
```

To use `fxReplay` with a transformer over a multi-shot monad, call it on the transformer's instance:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.statet.StateT
import com.github.kotlinz.kotlinz.data.statet.StateTMonad
import com.github.kotlinz.kotlinz.syntax.fxReplay

val list = object : ListKMonad {}
val stateT = object : StateTMonad<ListK.T, Int> { override val monad = list }
val program = StateT.narrow(stateT.fxReplay {
  val a = StateT(list) { s: Int -> listKOf(Pair(1, s + 1), Pair(2, s + 2)) }.bind()
  a * 10
})
check(program.run(0) == listKOf(Pair(10, 1), Pair(20, 2)))
```

## Caveats

- **Pick the right variant.** Calling the one-shot `fx` on a multi-shot monad (for example
  `object : ListKMonad {}.fx { ... }`) does not produce a meaningful result and can fail at runtime with a
  `ClassCastException`. The companion helpers already choose the right variant.
- **`fxReplay` is quadratic.** A block with `n` binds re-runs its prefix for every bound value.
- **Not stack-safe for long blocks.** Every `bind()` nests the rest of the block inside the monad's `bind`, so a
  block that binds many times (for example `repeat(100_000) { m.bind() }`) can throw `StackOverflowError` even for
  `Identity` or `Maybe`. Use `MonadRec.tailRecM` or `Free` interpreted into a `MonadRec` for long loops (see
  [MonadRec](type-classes/functor-applicative-monad.md#monadrec)).
- **Restricted suspension.** `MonadScope` is annotated with `@RestrictsSuspension`, so only `.bind()` can be called
  inside the block; other suspending functions (`delay`, `withContext`, ...) are not allowed. `fx` is not an
  integration with `kotlinx.coroutines`.
- **Exceptions** thrown inside the block propagate like in ordinary code, except where the monad itself catches
  them (`Try.fx` turns them into `Failure`).

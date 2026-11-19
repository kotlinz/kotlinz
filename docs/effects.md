# Effects

Kotlinz keeps side effects as ordinary values: an effect is *described* first and *run* later, so programs can be
composed, retried and tested. This page covers the effect types built on top of `IO` (`Resource`, `Ref`) and the
pure "effect-like" monads for parsing, random generation and backtracking search (`Parser`, `Gen`, `Logic`).

| Type | Package | What it models | Type classes |
|---|---|---|---|
| `IO<A>` | `data.io` | a suspended side effect | `Monad`, `MonadError<Throwable>`, `MonadIO` |
| `Resource<A>` | `data.resource` | acquire / use / release with guaranteed cleanup | `Functor`, `Applicative`, `Monad` |
| `Ref<A>` | `data.ref` | an atomic mutable cell whose reads and writes are `IO` | (none; a plain class) |
| `Parser<A>` | `data.parser` | a backtracking string parser with position-aware errors | `Monad`, `Alternative`, `MonadPlus` |
| `Gen<A>` | `data.gen` | a pure, seeded random generator | `Functor`, `Applicative`, `Monad` |
| `LogicT<F, A>`, `Logic` | `data.logict` | backtracking search with fair choice and pruning | `MonadPlus`, `MonadLogic`, `MonadTrans`, `MonadIO` |

All of them have `fmap` / `ap` / `bind` as members and `pure`, `join` and `fx` on their companion (or on the
`Logic` object).

## IO

`IO { ... }` suspends a computation; nothing happens until `unsafeRun()`, and every run re-executes the effect.
Exceptions are surfaced through `MonadError<Throwable>` (`handleError`, `handleErrorWith`, `attempt`). See
[Data types](data-types.md#io) for the basics.

`MonadIO<T>` is the class of monads that can embed an `IO` with `liftIO`. `IOMonadIO` is the trivial instance, and
every transformer gets one when its base monad has it, so `IO` can be lifted through a whole stack in one step (see
[Monad transformers](transformers.md#monadtrans-mfunctor-and-monadio)).

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO

var runs = 0
val effect = IO { runs += 1; runs }
val lifted = IO.narrow(object : IOMonadIO {}.liftIO(effect))

check(runs == 0)
check(lifted.unsafeRun() == 1)
check(effect.unsafeRun() == 2)
check(IO.raiseError<Int>(IllegalStateException("boom")).attempt().unsafeRun() is Either.Left)
```

## Resource

`Resource<A>` pairs an acquisition with its release. `Resource.make(acquire, release)` builds one from an `IO` and a
release function, and `use(f)` acquires the value, runs `f`, and **always** releases it, even when `f` throws.
Resources composed with `bind` (or `fx`) are acquired in order and released in reverse order. If acquiring a later
resource fails, the earlier ones are still released; if a release fails after another failure, its exception is
attached to the original one as suppressed. `Resource.liftIO` embeds an `IO` that needs no release.

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.resource.Resource

val events = mutableListOf<String>()
fun open(name: String): Resource<String> =
  Resource.make(IO { events += "open $name"; name }) { n -> IO { events += "close $n" } }

val both = Resource.fx { Pair(open("a").bind(), open("b").bind()) }
val used = both.use { (a, b) -> IO { events += "use $a$b"; a + b } }

check(events.isEmpty())
check(used.unsafeRun() == "ab")
check(events == listOf("open a", "open b", "use ab", "close b", "close a"))
```

The release also runs when the use fails:

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.resource.Resource

val events = mutableListOf<String>()
val file = Resource.make(IO { events += "open"; "x" }) { IO { events += "close" } }
val failed = file.use<Int> { IO { throw IllegalStateException("boom") } }

check(failed.attempt().unsafeRun() is Either.Left)
check(events == listOf("open", "close"))
```

## Ref

`Ref<A>` is an atomic reference (backed by `AtomicReference`) whose operations are `IO` effects. `Ref.of(initial)`
returns an `IO<Ref<A>>`, so every run of the program allocates a fresh cell; `Ref.unsafeOf` creates one
immediately. `update` / `modify` retry with compare-and-set, so the update function may run more than once under
contention and should be pure.

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.ref.Ref

val program = IO.fx {
  val counter = Ref.of(0).bind()
  counter.update { it + 1 }.bind()
  val previous = counter.getAndUpdate { it * 10 }.bind()
  val label = counter.modify { n -> Pair(n + 1, "was $n") }.bind()
  Triple(previous, label, counter.get().bind())
}

check(program.unsafeRun() == Triple(1, "was 10", 11))
check(program.unsafeRun() == Triple(1, "was 10", 11))
```

## Parser

`Parser<A>` is a function from the input string and a start offset to a `ParseResult<A>`: either
`ParseResult.Success(value, position)` or `ParseResult.Failure(ParseError(position, expected))`. It is a `Monad` and a
`MonadPlus`; `orElse` backtracks fully and the error that got **furthest** wins, with the expectations of
alternatives at the same offset merged.

Primitives live on the companion: `satisfy`, `char`, `string`, `anyChar`, `eof`, `fail`, `defer` (for recursive
grammars), `many`, `some`, `sepBy`, `sepBy1`, `choice`. Members add `keepLeft` / `keepRight`, `optional` and
`label`. `parse` allows trailing input; `parseAll` requires the whole input to be consumed.
`ParseError.describe(input)` renders a 1-based line and column. A `Success` may also carry a `hint`: the furthest
failure seen on the way (for example the attempt that ended a `many`), which is merged into the error if parsing
fails right afterwards.

```kotlin
import com.github.kotlinz.kotlinz.data.parser.ParseError
import com.github.kotlinz.kotlinz.data.parser.ParseResult
import com.github.kotlinz.kotlinz.data.parser.Parser

val digit = Parser.satisfy("digit") { it.isDigit() }
val number = Parser.some(digit) fmap { it.joinToString("").toInt() }
val list = Parser.char('[') keepRight Parser.sepBy(number, Parser.char(',')) keepLeft Parser.char(']')

check(list.parseAll("[1,22,3]") == ParseResult.Success(listOf(1, 22, 3), 8))

val error = (list.parseAll("[1,x]") as ParseResult.Failure).error
check(error == ParseError(3, setOf("digit")))
check(error.describe("[1,x]") == "line 1, column 4: expected digit")
```

Recursive grammars are tied with `Parser.defer`, and `fx` works because a parser yields at most one result per run:

```kotlin
import com.github.kotlinz.kotlinz.data.parser.ParseResult
import com.github.kotlinz.kotlinz.data.parser.Parser

val number = Parser.some(Parser.satisfy("digit") { it.isDigit() }) fmap { it.joinToString("").toInt() }
lateinit var expr: Parser<Int>
val atom: Parser<Int> = number orElse (Parser.char('(') keepRight Parser.defer { expr } keepLeft Parser.char(')'))
val sum: Parser<Int> = Parser.fx {
  val head = atom.bind()
  val tail = Parser.many(Parser.char('+') keepRight atom).bind()
  head + tail.sum()
}

expr = sum
check((expr.parseAll("1+(2+3)+4") as ParseResult.Success).value == 10)
check(expr.parseAll("1+") is ParseResult.Failure)
```

## Gen

`Gen<A>` is a pure random generator: a `State` computation over an immutable SplitMix64 `Seed`. The same seed
always produces the same value, so random programs are reproducible. `run(seed)` returns the value with the advanced
seed; `sample(long)` is a shortcut. Primitives: `long`, `int`, `int(range)`, `boolean`, `double`, `choose(min, max)`;
combinators: `elements`, `oneOf`, `listOf(size, gen)` and `listOf(sizeGen, gen)`.

```kotlin
import com.github.kotlinz.kotlinz.data.gen.Gen
import com.github.kotlinz.kotlinz.data.gen.Seed

val point = Gen.fx { Pair(Gen.choose(0, 9).bind(), Gen.boolean().bind()) }
val dice = Gen.listOf(5, Gen.int(1..6))

check(point.sample(42L) == point.sample(42L))
check(point.sample(42L).first in 0..9)
check(dice.sample(7L).size == 5 && dice.sample(7L).all { it in 1..6 })

val (first, next) = Gen.long().run(Seed(1L))
check(Gen.long().run(Seed(1L)) == Pair(first, next))
check(Gen.long().run(next).first != first)
```

## Logic and LogicT

`LogicT<F, A>` is a backtracking search over a base monad `F` (Kiselyov et al., "Backtracking, Interleaving, and
Terminating Monad Transformers"); `Logic` is the pure variant over `Identity`. Besides depth-first `bind` / `orElse`
it implements `MonadLogic`, whose operations are all derived from `msplit` (split off the first result):

| Operation | Meaning |
|---|---|
| `interleave` | fair disjunction: alternate the results of two searches, so an infinite one cannot starve the other |
| `fairBind` | fair conjunction: interleave the results produced for each value |
| `ifte(then, otherwise)` | soft cut: bind every result to `then`, or run `otherwise` if there is none |
| `once()` | keep only the first result |
| `lnot()` | negation as failure: succeed once exactly when there is no result |

Results are observed with `observe()` / `observeAll()` / `observeMany(n)` for `Logic`, and `observeT()` /
`observeAllT()` / `observeManyT(n)` (returning `K1<F, ...>`) for `LogicT`. Infinite searches must be built with
`defer` and consumed with `observeMany`.

```kotlin
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.logict.Logic
import com.github.kotlinz.kotlinz.data.logict.LogicT
import com.github.kotlinz.kotlinz.data.logict.observe
import com.github.kotlinz.kotlinz.data.logict.observeAll
import com.github.kotlinz.kotlinz.data.logict.observeMany
import com.github.kotlinz.kotlinz.data.maybe.Maybe

fun nats(n: Int): LogicT<Identity.T, Int> = Logic.pure(n) orElse Logic.defer { nats(n + 1) }

val triples = Logic.fx {
  val a = Logic.of(1, 2, 3, 4, 5).bind()
  val b = Logic.of(1, 2, 3, 4, 5).bind()
  val c = Logic.of(1, 2, 3, 4, 5).bind()
  if (a <= b && a * a + b * b == c * c) Triple(a, b, c) else Logic.empty<Triple<Int, Int, Int>>().bind()
}

check(triples.observeAll() == listOf(Triple(3, 4, 5)))
check((nats(0) interleave Logic.of(-1, -2)).observeMany(4) == listOf(0, -1, 1, -2))
check(nats(10).once().observeAll() == listOf(10))
check(Logic.empty<Int>().lnot().observe() == Maybe.Just(Unit))
check(Logic.of(1, 2).ifte({ Logic.pure(it * 10) }, Logic.pure(0)).observeAll() == listOf(10, 20))
```

Over another base monad, the base effects run as the search proceeds; `LogicT.lift` embeds a base computation:

```kotlin
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.logict.LogicT

val io = object : IOMonad {}
val seen = mutableListOf<Int>()
val search = LogicT.of(io, 1, 2, 3) bind { x -> LogicT.lift(io, IO { seen += x; x * x }) }

check(IO.narrow(search.observeManyT(2)).unsafeRun() == listOf(1, 4))
check(seen == listOf(1, 2))
```

`fx` on `LogicT` / `Logic` uses `fxReplay` (see [fx](fx.md#multi-shot-monads-fxreplay)), so code between binds
re-runs for every result.

## Limitations

- `IO`, `Resource` and `LogicT` are not stack-safe for very deep `bind` chains.
- `IO` has no concurrency, cancellation or asynchronous boundaries; it is a synchronous thunk.
- `Ref` operations are atomic individually, but a sequence of them is not a transaction.
- `Gen` has no shrinking; it is a generator, not a property-testing framework.

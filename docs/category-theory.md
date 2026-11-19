# Category-theory constructs

This guide covers the more abstract building blocks: natural transformations, adjunctions, the Yoneda family,
Kan extensions, Day convolution, free structures, fixed points and recursion schemes. They all rely on the encoding
described in [Higher-kinded types](hkt.md).

| Construct | Package | Instances / entry points |
|---|---|---|
| `NaturalTransformation<F, G>` | `type.transformation` | `identity`, `compose`, `andThen`; `MaybeToListK`, `ListKToMaybe`, `EitherToMaybe` |
| `Adjunction<F, G>` | `type.adjunction` | `EnvReaderAdjunction<S>` |
| `Yoneda<F, A>` / `Coyoneda<F, A>` | `data.yoneda` / `data.coyoneda` | `Functor` |
| `Codensity<F, A>` / `Density<F, A>` | `data.codensity` / `data.density` | `Monad` / `Comonad` |
| `Ran<G, H, A>` / `Lan<G, H, A>` | `data.kan` | `Functor`; `toRan` / `fromRan` / `gran`, `toLan` / `fromLan` / `glan` |
| `Day<F, G, A>` | `data.day` | `Functor`, `Applicative`, `Comonad`; `dap`, `swapped`, `trans1`, `trans2` |
| `Free<F, A>` | `data.free` | `Monad`; `liftF`, `foldMap`, `fx` |
| `Freer<F, A>` | `data.freer` | `Monad`; `liftF` / `singleton`, `foldMap` / `interpret`, `view`, `fx` |
| `FreeAp<F, A>` | `data.freeap` | `Applicative`; `lift`, `foldMap`, `analyze`, `hoist`, `retract` |
| `FreeSelective<F, A>` | `data.freeselective` | `Selective`; `lift`, `foldMap`, `effects`, `necessaryEffects`, `analyze`, `hoist`, `retract` |
| `FreeCategory<P, A, B>` / `FreeArrow<P, A, B>` | `data.freecategory` / `data.freearrow` | `Category` / `Arrow`; `lift`, `foldMap` with a `QuiverTransformation` |
| `Cofree<F, A>` | `data.cofree` | `Comonad`; see [Comonads](comonads.md#cofree) |
| `Fix<F>` | `data.fix` | `cata`, `ana`, `hylo`, `para`, `apo`, `histo`, `zygo`, `prepro`, `elgot`, `futu`, `postpro`, `coelgot` |
| `Mu<F>` / `Nu<F>` | `data.fix` | least / greatest fixed point; `cata`, `ana` / `unfold`, `embed`, `project`, conversions |

Free monad transformers, comonad transformers, profunctor constructions and the co-Kleisli arrow are covered
elsewhere; see [Related constructs in other guides](#related-constructs-in-other-guides).

## Natural transformations

A `NaturalTransformation<F, G>` converts `F<A>` into `G<A>` for every `A`. Because the method is generic, it has
to be implemented with an `object` rather than a lambda.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.transformation.EitherToMaybe
import com.github.kotlinz.kotlinz.type.transformation.ListKToMaybe
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

val reversed = object : NaturalTransformation<ListK.T, ListK.T> {
  override fun <A> invoke(fa: K1<ListK.T, A>): ListK<A> = ListK(ListK.narrow(fa).value.reversed())
}
val last = reversed andThen ListKToMaybe

check(ListKToMaybe(listKOf(1, 2, 3)) == Maybe.Just(1))
check(last(listKOf(1, 2, 3)) == Maybe.Just(3))
check(MaybeToListK(Maybe.Just("x")) == listKOf("x"))
check(EitherToMaybe<String>()(Either.Left<String, Int>("e")) == Maybe.None<Int>())
```

Natural transformations are the interpreters of `Free`, `FreeAp` and `Coyoneda` below.

## Adjunction

<!-- nocheck -->
```kotlin
interface Adjunction<F, G> {
  val left: Functor<F>
  val right: Functor<G>
  fun <A> unit(a: A): K1<G, K1<F, A>>
  fun <A> counit(fga: K1<F, K1<G, A>>): A
  // leftAdjunct, rightAdjunct, joinComposed, duplicateComposed have default implementations
}
```

The provided instance is `EnvReaderAdjunction<S>`: `Env<S, _>` is left adjoint to `Reader<S, _>`. Composing them
as `Reader<S, Env<S, A>>` gives `State<S, A>`, and as `Env<S, Reader<S, A>>` gives `Store<S, A>`; the conversion
functions `toReaderEnv` / `toState` and `toEnvReader` / `toStore` witness these isomorphisms.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvReaderAdjunction
import com.github.kotlinz.kotlinz.data.env.toReaderEnv
import com.github.kotlinz.kotlinz.data.env.toState
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.state.State

val adjunction = object : EnvReaderAdjunction<String> {}

check(Reader.narrow(adjunction.unit(42)).run("ctx") == Env("ctx", 42))
check(adjunction.counit(Env<String, K1<K1<Reader.T, String>, Int>>("abc", Reader { s: String -> s.length })) == 3)

val describe = adjunction.leftAdjunct { e: K1<K1<Env.T, String>, Int> -> Env.narrow(e).let { "${it.env}:${it.value}" } }
check(Reader.narrow(describe(1)).run("x") == "x:1")

val counter = State { s: Int -> Pair(s * 2, s + 1) }
check(counter.toReaderEnv().toState().run(3) == Pair(6, 4))
```

## Yoneda and Coyoneda

`Yoneda<F, A>` is `forall B. ((A) -> B) -> F<B>`. Lifting into `Yoneda` and calling `fmap` repeatedly composes the
functions instead of traversing the structure each time; `lower()` applies them in a single `fmap`.

`Coyoneda<F, A>` is the *free functor*: it pairs an `F<B>` with a function `(B) -> A`, so it is a `Functor` even
when `F` is not. A real `Functor<F>` is only needed in `lower(functor)`, and `hoist` applies a natural
transformation to the underlying value.

```kotlin
import com.github.kotlinz.kotlinz.data.coyoneda.Coyoneda
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.data.yoneda.Yoneda
import com.github.kotlinz.kotlinz.type.transformation.ListKToMaybe

var calls = 0
val yoneda = Yoneda.lift(object : ListKFunctor {}, listKOf(1, 2, 3)) fmap { calls += 1; it + 1 } fmap { it * 2 }
check(calls == 0)
check(yoneda.lower() == listKOf(4, 6, 8))

val coyoneda = Coyoneda.lift(listKOf(1, 2, 3)) fmap { it * 10 }
check(coyoneda.lower(object : ListKFunctor {}) == listKOf(10, 20, 30))
check(coyoneda.hoist(ListKToMaybe).lower(object : MaybeFunctor {}) == Maybe.Just(10))
```

## Codensity and Density

`Codensity<F, A>` (`forall R. ((A) -> F<R>) -> F<R>`) is a monad for *any* `F`. Lifting a monadic computation into
`Codensity` re-associates binds to the right, which speeds up left-nested binds (for example in free-monad-like
code). `lower` needs only a `Pointed<F>`.

```kotlin
import com.github.kotlinz.kotlinz.data.codensity.Codensity
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.listk.listKOf

val list = object : ListKMonad {}
val pairs = Codensity.lift(list, listKOf(1, 2)) bind { x -> Codensity.lift(list, listKOf(x, x * 10)) }
check(ListK.narrow(pairs.lower(list)) == listKOf(1, 10, 2, 20))
```

`Density<F, A>` is its dual, a comonad for any `F`; see [Comonads](comonads.md#density).

## Kan extensions

`Ran<G, H, A>` (right Kan extension, `forall B. ((A) -> G<B>) -> H<B>`) and `Lan<G, H, A>` (left Kan extension,
`exists B. ((G<B>) -> A, H<B>)`) are `Functor`s. Their universal properties are provided as functions:
`toRan` / `fromRan` relate `K ∘ G ~> H` to `K ~> Ran<G, H>`, and `toLan` / `fromLan` relate `Lan<G, H> ~> F` to
`H ~> F ∘ G`. Natural transformations out of or into a composition are expressed with the `FromComposition` and
`IntoComposition` interfaces.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.kan.FromComposition
import com.github.kotlinz.kotlinz.data.kan.IntoComposition
import com.github.kotlinz.kotlinz.data.kan.Lan
import com.github.kotlinz.kotlinz.data.kan.Ran
import com.github.kotlinz.kotlinz.data.kan.toLan
import com.github.kotlinz.kotlinz.data.kan.toRan
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe

// ListK ∘ Maybe ~> ListK
val catMaybes = object : FromComposition<ListK.T, Maybe.T, ListK.T> {
  override fun <A> invoke(v: K1<ListK.T, K1<Maybe.T, A>>): ListK<A> =
    ListK(ListK.narrow(v).value.mapNotNull { (Maybe.narrow(it) as? Maybe.Just)?.value })
}
val ran = Ran.narrow(toRan(object : ListKFunctor {}, catMaybes)(listKOf(1, 2, 3, 4)))
check(ran.runRan { n -> if (n % 2 == 0) Maybe.Just(n * 10) else Maybe.None() } == listKOf(20, 40))

// ListK ~> ListK ∘ Maybe
val wrapJust = object : IntoComposition<ListK.T, ListK.T, Maybe.T> {
  override fun <A> invoke(v: K1<ListK.T, A>): ListK<K1<Maybe.T, A>> = ListK(ListK.narrow(v).value.map { Maybe.Just(it) })
}
val lan = Lan({ m: K1<Maybe.T, Int> -> Maybe.narrow(m).getOrElse(0) * 2 }, listKOf(1, 2, 3)) fmap { it + 1 }
check(toLan(object : ListKFunctor {}, wrapJust)(lan) == listKOf(3, 5, 7))
```

## Day convolution

`Day<F, G, A>` pairs an `F<B>` and a `G<C>` with a function `(B, C) -> A`. It is a `Functor` for any `F` and `G`,
an `Applicative` when both are applicatives, and a `Comonad` when both are comonads. When `F == G` is an
applicative, `dap` collapses it.

```kotlin
import com.github.kotlinz.kotlinz.data.day.Day
import com.github.kotlinz.kotlinz.data.day.dap
import com.github.kotlinz.kotlinz.data.env.Env
import com.github.kotlinz.kotlinz.data.env.EnvComonad
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityComonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad

val product = Day(Maybe.Just(2), Maybe.Just(3)) { b: Int, c: Int -> b * c } fmap { it + 1 }
check(product.dap(object : MaybeMonad {}) == Maybe.Just(7))

val comonad = Day.comonad(object : IdentityComonad {}, object : EnvComonad<String> {})
check(comonad.extract(Day(Identity(3), Env("env", 4)) { b: Int, c: Int -> b + c }) == 7)
```

## Free monad

`Free<F, A>` turns any type constructor `F` (no `Functor` required) into a monad. You describe a program as data
and interpret it later with a natural transformation into a real monad via `foldMap(nt, monad)`. `Free.fx`
provides do-notation; it uses `fxReplay`, so multi-shot interpreters such as `ListK` also work.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.free.Free
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.StateMonadRec
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

sealed class KV<A> : K1<KV.T, A> {
  class T
  class Store(val key: String, val value: Int) : KV<Unit>()
  class Load(val key: String) : KV<Int>()
}

fun store(key: String, value: Int): Free<KV.T, Unit> = Free.liftF(KV.Store(key, value))
fun load(key: String): Free<KV.T, Int> = Free.liftF(KV.Load(key))

val program: Free<KV.T, Int> = Free.fx {
  store("a", 1).bind()
  store("b", 2).bind()
  load("a").bind() + load("b").bind()
}

typealias Memory = Map<String, Int>

val toState = object : NaturalTransformation<KV.T, K1<State.T, Memory>> {
  @Suppress("UNCHECKED_CAST")
  override fun <A> invoke(fa: K1<KV.T, A>): State<Memory, A> = when (val op = fa as KV<A>) {
    is KV.Store -> State { m: Memory -> Pair(Unit as A, m + (op.key to op.value)) }
    is KV.Load -> State { m: Memory -> Pair(m.getValue(op.key) as A, m) }
  }
}

val stateMonad = object : StateMonadRec<Memory> {}
check(State.narrow(program.foldMap(toState, stateMonad)).run(emptyMap()) == Pair(3, mapOf("a" to 1, "b" to 2)))

// Interpreting into a MonadRec is stack-safe, even for 100k left-nested binds
val many = (1..100_000).fold(Free.pure<KV.T, Unit>(Unit)) { acc, i -> acc bind { store("last", i) } }
check(State.narrow(many.foldMap(toState, stateMonad)).exec(emptyMap()) == mapOf("last" to 100_000))
```

`foldMap` is stack-safe when the target monad implements `MonadRec` (`Identity`, `Eval`, `State`); otherwise stack
safety depends on the target's `bind`.

## Free applicative

`FreeAp<F, A>` is the free `Applicative`. Because applicative effects cannot depend on earlier results, the
program's structure is static and can be *analyzed* without running it, for example with `analyze`, which folds
every instruction into a monoid through `Const`:

<!-- continue -->
```kotlin
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.freeap.FreeAp
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid

val sum: FreeAp<KV.T, Int> =
  FreeAp.lift(KV.Load("b")) ap (FreeAp.lift(KV.Load("a")) fmap { a: Int -> { b: Int -> a + b } })

val describeOp = object : NaturalTransformation<KV.T, K1<Const.T, ListMonoid>> {
  @Suppress("UNCHECKED_CAST")
  override fun <A> invoke(fa: K1<KV.T, A>): Const<ListMonoid, A> = when (val op = fa as KV<A>) {
    is KV.Store -> Const(ListMonoid(listOf("store ${op.key}")))
    is KV.Load -> Const(ListMonoid(listOf("load ${op.key}")))
  }
}

check(sum.analyze(ListMonoid::class.java, describeOp) == ListMonoid(listOf("load a", "load b")))
check(State.narrow(sum.foldMap(toState, stateMonad)).eval(mapOf("a" to 1, "b" to 2)) == 3)
```

(This block continues the previous one and reuses `KV`, `toState` and `stateMonad`.) In `x ap f`, the effects of
`f` run before those of `x`.

## Freer monad

`Freer<F, A>` is the *operational* (freer) monad: like `Free`, it needs no `Functor` for the instruction set, but
it stores continuations in a type-aligned queue ("reflection without remorse"), so `bind` is O(1) and both building
and stepping through left-nested programs are stack-safe. Interpret it with `foldMap` / `interpret` into any monad
(stack-safe into a `MonadRec`), or write a custom interpreter by pattern matching on `view()`, which exposes the
next instruction and its continuation:

<!-- continue -->
```kotlin
import com.github.kotlinz.kotlinz.data.freer.Freer
import com.github.kotlinz.kotlinz.data.freer.FreerView

val freerProgram: Freer<KV.T, Int> = Freer.fx {
  Freer.liftF(KV.Store("a", 20)).bind()
  Freer.liftF(KV.Load("a")).bind() * 2 + 2
}

check(State.narrow(freerProgram.interpret(toState, stateMonad)).eval(emptyMap()) == 42)

// A pure interpreter written directly against view()
@Suppress("UNCHECKED_CAST")
tailrec fun <A> runPure(program: Freer<KV.T, A>, memory: Memory): A = when (val step = program.view()) {
  is FreerView.Return -> step.value
  is FreerView.Bind<KV.T, *, A> -> {
    val bind = step as FreerView.Bind<KV.T, Any?, A>
    when (val op = bind.instruction as KV<*>) {
      is KV.Store -> runPure(bind.continuation(Unit), memory + (op.key to op.value))
      is KV.Load -> runPure(bind.continuation(memory.getValue(op.key)), memory)
    }
  }
}

check(runPure(freerProgram, emptyMap()) == 42)
```

## Free selective functor

`FreeSelective<F, A>` sits between `FreeAp` (no branching) and `Free` (arbitrary, opaque branching): a program is a
chain of instructions where each may be skipped depending on earlier results, built with the `Selective`
combinators (`select`, `branch`, `ifS`, `whenS`; see [Selective](type-classes/selective.md)). Every *possible*
instruction is still known up front, so `effects()` lists all instructions that may run and `necessaryEffects()`
those that always run. `foldMap` interprets it into any `Selective` (for a monad, mix in `MonadSelective`) and runs
only the instructions on the branch taken.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.freeselective.FreeSelective
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeSelective
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

class Read<A>(val key: String, val parse: (String) -> A) : K1<Read.T, A> {
  class T
}

fun <A> read(key: String, parse: (String) -> A): FreeSelective<Read.T, A> = FreeSelective.lift(Read(key, parse))

fun config(values: Map<String, String>) = object : NaturalTransformation<Read.T, Maybe.T> {
  @Suppress("UNCHECKED_CAST")
  override fun <A> invoke(fa: K1<Read.T, A>): Maybe<A> =
    (fa as Read<A>).let { r -> values[r.key]?.let { Maybe.Just(r.parse(it)) } ?: Maybe.None() }
}

val s = FreeSelective.selective<Read.T>()
val logLevel = FreeSelective.narrow(s.ifS(read("verbose") { it == "yes" }, read("level") { it }, FreeSelective.pure("quiet")))

check(logLevel.effects().map { (it as Read<*>).key } == listOf("verbose", "level"))
check(logLevel.necessaryEffects().map { (it as Read<*>).key } == listOf("verbose"))

val maybe = object : MaybeSelective {}
check(Maybe.narrow(logLevel.foldMap(config(mapOf("verbose" to "no")), maybe)) == Maybe.Just("quiet"))
check(Maybe.narrow(logLevel.foldMap(config(mapOf("verbose" to "yes", "level" to "debug")), maybe)) == Maybe.Just("debug"))
check(Maybe.narrow(logLevel.foldMap(config(mapOf("verbose" to "yes")), maybe)) == Maybe.None<String>())
```

`analyze(monoidClass, nt)` folds every possible instruction into a monoid through `Const`, as with `FreeAp`.

## Free category and free arrow

`FreeCategory<P, A, B>` is the free category over a *quiver* `P`: a type of abstract arrows encoded as
`K2<P, A, B>`. Paths are built from `id`, `lift` and `andThen` / `compose`, and interpreted with
`foldMap(category, nt)`, where `nt` is a `QuiverTransformation<P, T, F>` mapping each quiver arrow to an arrow of
the target `Category<T, F>`. `FreeArrow<P, A, B>` additionally embeds plain functions (`arr`) and supports `first`,
`second`, `split` and `combine`; it is interpreted into any `Arrow`. The same description can be run by different
interpreters, for example to execute it or to list its steps.

```kotlin
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.freearrow.FreeArrow
import com.github.kotlinz.kotlinz.data.freecategory.FreeCategory
import com.github.kotlinz.kotlinz.data.freecategory.QuiverTransformation
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Arrow
import com.github.kotlinz.kotlinz.data.function.Function1Category

sealed class Step<A, B> : K2<Step.T, A, B> {
  class T
  object Parse : Step<String, Int>()
  object Twice : Step<Int, Int>()
  object Show : Step<Int, String>()
}

object StepToFunction1 : QuiverTransformation<Step.T, Function1.T, Unit> {
  @Suppress("UNCHECKED_CAST")
  override fun <A, B> invoke(arrow: K2<Step.T, A, B>): K3<Function1.T, Unit, A, B> = when (arrow as Step<A, B>) {
    Step.Parse -> Function1 { s: String -> s.toInt() }
    Step.Twice -> Function1 { n: Int -> n * 2 }
    Step.Show -> Function1 { n: Int -> "#$n" }
  } as K3<Function1.T, Unit, A, B>
}

object StepNames : QuiverTransformation<Step.T, Function1.T, Unit> {
  @Suppress("UNCHECKED_CAST")
  override fun <A, B> invoke(arrow: K2<Step.T, A, B>): K3<Function1.T, Unit, A, B> =
    Function1 { log: List<String> -> log + arrow::class.simpleName!! } as K3<Function1.T, Unit, A, B>
}

val pipeline = FreeCategory.lift(Step.Parse) andThen FreeCategory.lift(Step.Twice) andThen FreeCategory.lift(Step.Show)
check(Function1.narrow(pipeline.foldMap(object : Function1Category {}, StepToFunction1))("21") == "#42")

@Suppress("UNCHECKED_CAST")
val steps = Function1.narrow(pipeline.foldMap(object : Function1Category {}, StepNames)) as Function1<List<String>, List<String>>
check(steps(emptyList()) == listOf("Parse", "Twice", "Show"))

val both = FreeArrow.lift(Step.Twice) split FreeArrow.arr { s: String -> s.length }
check(Function1.narrow(both.foldMap(object : Function1Arrow {}, StepToFunction1))(Pair(2, "ab")) == Pair(4, 2))
```

The step-listing interpreter needs an unchecked cast, because a `QuiverTransformation` must preserve the arrow's
endpoint types while the log function works on `List<String>` regardless of them.

## Fix and recursion schemes

`Fix<F>` ties the knot of a non-recursive *base functor* `F`, and recursion schemes separate the recursion from the
per-layer logic:

| Scheme | Signature (simplified) | Purpose |
|---|---|---|
| `cata` | `(F<A>) -> A` ⟹ `(Fix<F>) -> A` | fold |
| `ana` | `(A) -> F<A>` ⟹ `(A) -> Fix<F>` | unfold |
| `hylo` | unfold then fold, without building the `Fix` | refold |
| `para` | `(F<Pair<Fix<F>, A>>) -> A` | fold with access to the original substructures |
| `apo` | `(A) -> F<Either<Fix<F>, A>>` | unfold that can stop early with a finished `Fix` |
| `histo` | `(F<Cofree<F, A>>) -> A` | fold that sees the results for *all* substructures (course-of-values) |
| `zygo` | helper `(F<B>) -> B` and `(F<Pair<B, A>>) -> A` | fold with an auxiliary fold computed alongside |
| `prepro` | natural transformation `F ~> F`, then `(F<A>) -> A` | fold that transforms deeper layers more often |
| `elgot` | `(A) -> Either<B, F<A>>`, then `(F<B>) -> B` | refold whose unfold may short-circuit with a result |
| `futu` | `(A) -> F<Free<F, A>>` | unfold that emits several layers per step |
| `postpro` | `(A) -> F<A>`, then `F ~> F` | unfold that transforms deeper layers more often |
| `coelgot` | `(A, () -> F<B>) -> B` and `(A) -> F<A>` | refold whose fold sees the seed and may skip unfolding further |

The schemes are top-level functions in `data.fix` (`cata(functor, algebra)` returns a function); the basic ones are
also members / companion functions on `Fix` (`fix.cata(...)`, `fix.para(...)`, `Fix.ana(...)`, `Fix.apo(...)`).
The type aliases `Algebra`, `Coalgebra`, `RAlgebra`, `RCoalgebra`, `CVAlgebra` and `CVCoalgebra` name the
per-layer functions.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.fix.Algebra
import com.github.kotlinz.kotlinz.data.fix.Coalgebra
import com.github.kotlinz.kotlinz.data.fix.Fix
import com.github.kotlinz.kotlinz.data.fix.RAlgebra
import com.github.kotlinz.kotlinz.data.fix.hylo
import com.github.kotlinz.kotlinz.type.Functor

// The base functor of a list of E, with the recursive position abstracted as A
sealed class ListF<E, A> : K2<ListF.T, E, A> {
  class T
  class Nil<E, A> : ListF<E, A>()
  class Cons<E, A>(val head: E, val tail: A) : ListF<E, A>()
  companion object {
    fun <E, A> narrow(v: K1<K1<T, E>, A>): ListF<E, A> = v as ListF<E, A>
  }
}

class ListFFunctor<E> : Functor<K1<ListF.T, E>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<ListF.T, E>, A>): ListF<E, B> = when (val l = ListF.narrow(v)) {
    is ListF.Nil -> ListF.Nil()
    is ListF.Cons -> ListF.Cons(l.head, f(l.tail))
  }
}

typealias IntListF = K1<ListF.T, Int>

val functor = ListFFunctor<Int>()
val countDown: Coalgebra<IntListF, Int> = { n -> if (n == 0) ListF.Nil() else ListF.Cons(n, n - 1) }
val sum: Algebra<IntListF, Int> = { l ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> 0
    is ListF.Cons -> x.head + x.tail
  }
}
val ascending: RAlgebra<IntListF, Boolean> = { l ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> true
    is ListF.Cons -> x.tail.second && when (val next = ListF.narrow(x.tail.first.unfix)) {
      is ListF.Nil -> true
      is ListF.Cons -> x.head <= next.head
    }
  }
}

val fourToOne: Fix<IntListF> = Fix.ana(functor, countDown, 4)
check(fourToOne.cata(functor, sum) == 10)
check(hylo(functor, sum, countDown)(100) == 5050)
check(!fourToOne.para(functor, ascending))
check(Fix.ana(functor, { n: Int -> if (n > 3) ListF.Nil() else ListF.Cons(n, n + 1) }, 1).para(functor, ascending))
```

The remaining schemes take the same shape. Continuing the example above (reusing `ListF`, `functor`, `sum`,
`countDown` and `fourToOne`):

<!-- continue -->
```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.fix.CVAlgebra
import com.github.kotlinz.kotlinz.data.fix.CVCoalgebra
import com.github.kotlinz.kotlinz.data.fix.elgot
import com.github.kotlinz.kotlinz.data.fix.futu
import com.github.kotlinz.kotlinz.data.fix.histo
import com.github.kotlinz.kotlinz.data.fix.zygo
import com.github.kotlinz.kotlinz.data.free.Free

val toList: Algebra<IntListF, List<Int>> = { l ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> emptyList()
    is ListF.Cons -> listOf(x.head) + x.tail
  }
}
val length: Algebra<IntListF, Int> = { l ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> 0
    is ListF.Cons -> x.tail + 1
  }
}

// histo: the n-th Fibonacci number, where n is the list length; each layer reads the two previous results
val fib: CVAlgebra<IntListF, Int> = { l ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> 0
    is ListF.Cons -> when (val before = ListF.narrow(x.tail.tail)) {
      is ListF.Nil -> 1
      is ListF.Cons -> x.tail.head + before.tail.head
    }
  }
}
check(histo(functor, fib)(Fix.ana(functor, countDown, 10)) == 55)

// zygo: weight every element by the length of the rest of the list (4*3 + 3*2 + 2*1 + 1*0)
val weighted = zygo(functor, length) { l: K1<IntListF, Pair<Int, Int>> ->
  when (val x = ListF.narrow(l)) {
    is ListF.Nil -> 0
    is ListF.Cons -> x.head * x.tail.first + x.tail.second
  }
}
check(weighted(fourToOne) == 20)

// elgot: stop unfolding at 2 and use 100 for the rest (5 + 4 + 3 + 100)
val shortCircuit = elgot(functor, sum) { n: Int ->
  if (n == 2) Either.Left(100) else Either.Right<Int, K1<IntListF, Int>>(countDown(n))
}
check(shortCircuit(5) == 112)

// futu: emit two layers per step
val pairs: CVCoalgebra<IntListF, Int> = { n ->
  if (n <= 0) ListF.Nil() else ListF.Cons(n, Free.liftF(ListF.Cons(n * 10, n - 1)))
}
check(futu(functor, pairs)(2).cata(functor, toList) == listOf(2, 20, 1, 10))
```

### Mu and Nu

`Fix` is one of three fixed-point encodings. `Mu<F>` is the *least* fixed point, Church-encoded as its own fold:
`mu.cata(algebra)` needs no functor and always terminates for values built from finite data. `Nu<F>` is the
*greatest* fixed point: a seed together with a coalgebra, expanded one layer at a time by `project(functor)`, so it
can describe infinite structures such as streams (folding a `Nu` only terminates for finite values). `embed` adds a
layer in front, and `fromFix` / `toFix` / `toMu` / `toNu` convert between the representations.

<!-- continue -->
```kotlin
import com.github.kotlinz.kotlinz.data.fix.Mu
import com.github.kotlinz.kotlinz.data.fix.Nu

val muList: Mu<IntListF> = Mu.ana(functor, countDown, 4)
check(muList.cata(sum) == 10)
check(muList.toFix().cata(functor, toList) == listOf(4, 3, 2, 1))

val naturals: Nu<IntListF> = Nu.unfold<IntListF, Int>({ n -> ListF.Cons(n, n + 1) }, 0)

fun take(n: Int, nu: Nu<IntListF>): List<Int> =
  if (n == 0) emptyList()
  else when (val layer = ListF.narrow(nu.project(functor))) {
    is ListF.Nil -> emptyList()
    is ListF.Cons -> listOf(layer.head) + take(n - 1, layer.tail)
  }

check(take(3, naturals) == listOf(0, 1, 2))
check(take(3, Nu.embed(functor, ListF.Cons(-1, naturals))) == listOf(-1, 0, 1))
check(Nu.unfold(countDown, 4).cata(functor, sum) == 10)
check(Nu.unfold(countDown, 3).toMu(functor).cata(sum) == 6)
```

Limitation: the recursion schemes are **not stack-safe**. A generic `Functor` offers no way to suspend inside
`fmap`, so every layer consumes a few JVM stack frames, and very deep structures (tens of thousands of layers,
depending on the stack size) throw `StackOverflowError`. The same applies to `Mu` / `Nu` folds and conversions.

## Related constructs in other guides

| Construct | Guide |
|---|---|
| `FreeT<F, M, A>` (free monad transformer), `MonadTrans` / `MFunctor` | [Monad transformers](transformers.md) |
| `CofreeT`, `EnvT`, `StoreT`, `TracedT`, `ComonadTrans` | [Comonads](comonads.md) |
| `Cokleisli<W, A, B>` (co-Kleisli arrows of a comonad), `Moore`, `Mealy`, `Fold` | [Automata](automata.md) |
| `Star`, `Costar`, `Forget`, `Tagged`, `Procompose`, `Cayley`, `Tambara` | [Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md) |
| Profunctor optics (`Iso`, `Lens`, `Prism`, `Optional`, `Traversal`, ...) | [Optics](optics.md) |

## Stack safety at a glance

- `Free.foldMap` and `Freer.foldMap` / `interpret` are stack-safe when the target monad implements `MonadRec`
  (`Identity`, `Eval`, `State`); building left-nested `Freer` programs is always stack-safe.
- `FreeAp`, `FreeSelective`, `FreeCategory` and `FreeArrow` interpreters recurse over the structure, so very large
  programs may overflow the stack.
- Recursion schemes and `Mu` / `Nu` folds are not stack-safe (see above).

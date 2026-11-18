# Category-theory constructs

This guide covers the more abstract building blocks: natural transformations, adjunctions, the Yoneda family,
Kan extensions, Day convolution, free structures and recursion schemes. They all rely on the encoding described in
[Higher-kinded types](hkt.md).

| Construct | Package | Instances / entry points |
|---|---|---|
| `NaturalTransformation<F, G>` | `type.transformation` | `identity`, `compose`, `andThen`; `MaybeToListK`, `ListKToMaybe`, `EitherToMaybe` |
| `Adjunction<F, G>` | `type.adjunction` | `EnvReaderAdjunction<S>` |
| `Yoneda<F, A>` / `Coyoneda<F, A>` | `data.yoneda` / `data.coyoneda` | `Functor` |
| `Codensity<F, A>` / `Density<F, A>` | `data.codensity` / `data.density` | `Monad` / `Comonad` |
| `Ran<G, H, A>` / `Lan<G, H, A>` | `data.kan` | `Functor`; `toRan` / `fromRan` / `gran`, `toLan` / `fromLan` / `glan` |
| `Day<F, G, A>` | `data.day` | `Functor`, `Applicative`, `Comonad`; `dap`, `swapped`, `trans1`, `trans2` |
| `Free<F, A>` | `data.free` | `Monad`; `liftF`, `foldMap`, `fx` |
| `FreeAp<F, A>` | `data.freeap` | `Applicative`; `lift`, `foldMap`, `analyze`, `hoist`, `retract` |
| `Cofree<F, A>` | `data.cofree` | `Comonad`; see [Comonads](comonads.md#cofree) |
| `Fix<F>` | `data.fix` | `cata`, `ana`, `hylo`, `para`, `apo` |

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

The schemes are top-level functions in `data.fix` (`cata(functor, algebra)` returns a function), and also members /
companion functions on `Fix` (`fix.cata(...)`, `Fix.ana(...)`). The type aliases `Algebra`, `Coalgebra`,
`RAlgebra` and `RCoalgebra` name the per-layer functions.

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

Limitation: the recursion schemes are **not stack-safe**. A generic `Functor` offers no way to suspend inside
`fmap`, so every layer consumes a few JVM stack frames, and very deep structures (tens of thousands of layers,
depending on the stack size) throw `StackOverflowError`.

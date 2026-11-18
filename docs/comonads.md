# Comonads

A comonad (`Copointed` + `extract`, `duplicate`, `extend`; see
[the type-class guide](type-classes/functor-applicative-monad.md#copointed-and-comonad)) is a value *in a context*
that can always be observed. `extend` recomputes every position from its surroundings, which makes comonads a good
fit for cellular automata, image filters, zippers and streams.

Besides the general-purpose instances (`Identity`, `Eval`, `NonEmptyList`, `Tuple2`, `Function0`), Kotlinz
provides these dedicated comonads:

| Type | Shape | Extra operations | Dual of |
|---|---|---|---|
| `Store<S, A>` | `(S) -> A` plus a position `S` | `seek`, `seeks`, `peek`, `peeks`, `experiment` | `State` |
| `Env<E, A>` | an environment `E` next to a value `A` | `ask`, `asks`, `local` | `Reader` |
| `Traced<M, A>` | `(M) -> A` for a monoid `M` | `trace`, `traces`, `listen`, `censor` | `Writer` |
| `Cofree<F, A>` | a value plus an `F` of more `Cofree`s (lazy) | `unfold`, `coiter`, `tail` | `Free` |
| `Density<F, A>` | a left Kan extension of `F` along itself | `lift`, `lower` | `Codensity` |
| `Day<F, G, A>` | Day convolution of two comonads | see [Category theory](category-theory.md#day-convolution) | |

All of them have `fmap`, `extract`, `duplicate` and `extend` as members.

## Store

A `Store` is a lookup function plus a cursor. `extend` lets each position look at its neighbours:

```kotlin
import com.github.kotlinz.kotlinz.data.store.Store

val cells = listOf(0, 1, 0, 1, 1)
val board = Store({ i: Int -> cells.getOrElse(i) { 0 } }, 0)

val neighbourSums = board extend { s -> Store.narrow(s).let { it.peeks { i -> i - 1 } + it.peeks { i -> i + 1 } } }

check(board.extract() == 0)
check(board.seek(3).extract() == 1)
check((0 until 5).map { neighbourSums.seek(it).extract() } == listOf(1, 0, 2, 1, 1))
```

## Env

`Env` is a value annotated with a read-only environment:

```kotlin
import com.github.kotlinz.kotlinz.data.env.Env

// env is a tax rate in percent
val priced = Env(env = 10, value = 200)
val withTax = priced extend { e -> Env.narrow(e).let { it.value * (100 + it.env) / 100 } }

check(withTax.extract() == 220)
check(withTax.ask() == 10)
check(priced.local { 20 }.asks { "$it%" } == "20%")
```

## Traced

`Traced<M, A>` is a function from a monoid `M`; `extract` applies it to the monoid's identity, and `trace` applies
it to any other value. Like `Writer`, it takes the monoid as a `Class<M>`.

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.traced.Traced

val position = Traced(SumMonoid::class.java) { offset: SumMonoid -> 100 + offset.value }

check(position.extract() == 100)
check(position.trace(SumMonoid(5)) == 105)

val lookAhead = position extend { t -> Traced.narrow(t).trace(SumMonoid(10)) }
check(lookAhead.trace(SumMonoid(1)) == 111)
```

## Cofree

`Cofree<F, A>` is a head value plus an `F` of further `Cofree`s. The tail is lazy, so infinite structures can be
built with `Cofree.unfold` / `Cofree.coiter`. `Cofree.comonad(functor)` returns the `Comonad` instance.

With `F = Identity` it is an infinite stream; with `F = Maybe` it is a non-empty list:

```kotlin
import com.github.kotlinz.kotlinz.data.cofree.Cofree
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor

val identity = object : IdentityFunctor {}
val naturals = Cofree.coiter(identity, 0) { n -> Identity(n + 1) }

fun take(stream: Cofree<Identity.T, Int>, n: Int): List<Int> =
  generateSequence(stream) { Identity.narrow(it.tail).value }.take(n).map { it.head }.toList()

val windowSums = Cofree.narrow(Cofree.comonad(identity).extend({ c -> take(Cofree.narrow(c), 2).sum() }, naturals))

check(take(naturals, 5) == listOf(0, 1, 2, 3, 4))
check(take(windowSums, 4) == listOf(1, 3, 5, 7))
```

## Density

`Density<F, A>` hides an `F<B>` together with a function `(F<B>) -> A`. `Density.lift(comonad, fa)` embeds a
comonad, and `lower(comonad)` turns it back:

```kotlin
import com.github.kotlinz.kotlinz.data.density.Density
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListComonad
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf

val comonad = object : NonEmptyListComonad {}
val density = Density.lift(comonad, nonEmptyListOf(1, 2, 3)) fmap { it * 10 }

check(density.extract() == 10)
check(NonEmptyList.narrow(density.lower(comonad)) == nonEmptyListOf(10, 20, 30))
```

## Relationship to the monads

`Store`, `Env` and `Traced` are the duals of `State`, `Reader` and `Writer`. In particular `Env<S, _>` is left
adjoint to `Reader<S, _>`; composing the two in one order gives `State` and in the other gives `Store`. See
[Adjunction](category-theory.md#adjunction) for `EnvReaderAdjunction` and the conversion functions
`toReaderEnv` / `toState` / `toEnvReader` / `toStore`.

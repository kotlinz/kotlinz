# Type classes

All type classes live under `com.github.kotlinz.kotlinz.type`. They are interfaces over a type-constructor tag
(see [Higher-kinded types](../hkt.md)), and every instance is a public interface named
`<DataType><TypeClass>` that you instantiate with `object : MaybeMonad {}`.

## Guides

| Guide | Type classes |
|---|---|
| [Functor, Applicative, Monad](functor-applicative-monad.md) | `Invariant`, `Functor`, `Pointed`, `Applicative`, `ApplicativeOps`, `Monad`, `MonadOps`, `Alternative`, `MonadPlus`, `MonadZip`, `MonadRec`, `Copointed`, `Comonad` |
| [Foldable and Traversable](foldable-traversable.md) | `Foldable`, `Traversable` |
| [Errors and MTL-style classes](mtl.md) | `ApplicativeError`, `MonadError`, `MonadReader`, `MonadState`, `MonadWriter` |
| [Contravariant functors](contravariant.md) | `Contravariant`, `Divisible`, `Decidable` |
| [Bifunctor, Profunctor and Arrow](bifunctor-profunctor-arrow.md) | `Bifunctor`, `Profunctor`, `Strong`, `Choice`, `Compose`, `Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `ArrowLoop` |
| [Distributive and Representable](distributive-representable.md) | `Distributive`, `Representable` |

`Adjunction` and `NaturalTransformation` are covered in [Category theory](../category-theory.md); `Semigroup`,
`Monoid`, `Group`, `Band`, `Semiring` and the lattices in [Monoids and algebraic structures](../monoids.md).

## Hierarchy

<!-- nocheck -->
```text
Invariant ─┬─ Functor ─┬─ Applicative (+ Pointed) ─┬─ Monad ─┬─ MonadPlus (+ Alternative)
           │           │                           │         ├─ MonadZip, MonadRec, MonadOps
           │           │                           │         ├─ MonadError (+ ApplicativeError)
           │           │                           │         └─ MonadReader, MonadState, MonadWriter
           │           │                           ├─ Alternative
           │           │                           └─ ApplicativeOps, ApplicativeError
           │           ├─ Traversable (+ Foldable)
           │           ├─ Comonad (+ Copointed)
           │           ├─ Distributive
           │           └─ Representable
           └─ Contravariant ─ Divisible ─ Decidable

Bifunctor
Profunctor ─┬─ Strong
            └─ Choice
Compose ─ Category ─ Arrow ─┬─ ArrowChoice
                            ├─ ArrowApply
                            └─ ArrowLoop (no instances)
```

## Coverage

Instances provided for each data type (✓). `F`/`A`/`M` = Functor / Applicative / Monad. Types marked with *
need a `Class<M>` for a `Monoid` or `Semigroup` (see [Higher-kinded types](../hkt.md#passing-monoids-and-semigroups-classm)).

| Data type | F | A | M | Foldable | Traversable | Comonad | Other |
|---|---|---|---|---|---|---|---|
| `Identity` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | `MonadZip`, `MonadRec`, `Distributive`, `Representable<Unit>` |
| `Maybe` | ✓ | ✓ | ✓ | ✓ | ✓ | | `Alternative`, `MonadPlus` |
| `Either<L, _>` | ✓ | ✓ | ✓ | ✓ | ✓ | | `MonadError<L>`, `Bifunctor` |
| `Try` | ✓ | ✓ | ✓ | ✓ | ✓ | | `MonadError<Throwable>` |
| `Validated<E, _>` * | ✓ | ✓ | | | | | error accumulation through `Semigroup<E>` |
| `Ior<A, _>` * | ✓ | ✓ | ✓ | | | | `Bifunctor` |
| `Eval` | ✓ | ✓ | ✓ | | | ✓ | `MonadRec` |
| `IO` | ✓ | ✓ | ✓ | | | | `MonadError<Throwable>` |
| `Reader<R, _>` | ✓ | ✓ | ✓ | | | | `MonadReader<R>`, `Distributive`, `Representable<R>` |
| `Writer<W, _>` * | ✓ | ✓ | ✓ | | | | `MonadWriter<W>`, `Bifunctor` |
| `State<S, _>` | ✓ | ✓ | ✓ | | | | `MonadState<S>`, `MonadRec` |
| `RWS<R, W, S, _>` * | ✓ | ✓ | ✓ | | | | `MonadReader`, `MonadWriter`, `MonadState` |
| `Cont<R, _>` | ✓ | ✓ | ✓ | | | | `callCC` |
| `ListK` | ✓ | ✓ | ✓ | ✓ | ✓ | | `Alternative`, `MonadPlus`, `MonadZip` |
| `NonEmptyList` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `Tuple2<S, _>` * | ✓ | ✓ | ✓ | | | ✓ | `Bifunctor` |
| `Twin` | ✓ | | | | | | `Representable<Boolean>` |
| `Const<C, _>` * | ✓ | ✓ | | | | | `Contravariant` |
| `Function0` | ✓ | ✓ | ✓ | | | ✓ | |
| `Function1<R, _>` | ✓ | ✓ | ✓ | | | | `Category`, `Arrow`, `Profunctor` |
| `Kleisli<F, _, _>` | | | | | | | `Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `Profunctor`, `Strong`, `Choice` |
| `Predicate`, `Equivalence`, `Comparison` | | | | | | | `Contravariant`, `Decidable` |
| `Op<R, _>` | | | | | | | `Contravariant`, `Decidable` (needs `Monoid<R>`) |
| `MonoidOf`, `SemigroupOf` | | | | | | | `Invariant` |
| `Compose<F, G, _>`, `Product<F, G, _>` | ✓ | ✓ | | ✓ | | | |
| `Coproduct<F, G, _>` | ✓ | | | ✓ | | | |
| `MaybeT`, `ReaderT`, `StateT`, `WriterT`, `EitherT` | ✓ | ✓ | ✓ | | | | `MonadReader` / `MonadState` / `MonadWriter` / `MonadError` respectively |
| `Store`, `Env`, `Traced` *, `Cofree`, `Density` | ✓ | | | | | ✓ | |
| `Day<F, G, _>` | ✓ | ✓ | | | | ✓ | |
| `Free`, `Codensity` | ✓ | ✓ | ✓ | | | | |
| `FreeAp` | ✓ | ✓ | | | | | |
| `Yoneda`, `Coyoneda`, `Ran`, `Lan` | ✓ | | | | | | |

The data types themselves are described in [Data types](../data-types.md), [Monad transformers](../transformers.md),
[Comonads](../comonads.md) and [Category theory](../category-theory.md).

# Type classes

All type classes live under `com.github.kotlinz.kotlinz.type`. They are interfaces over a type-constructor tag
(see [Higher-kinded types](../hkt.md)), and every instance is a public interface named
`<DataType><TypeClass>` that you instantiate with `object : MaybeMonad {}`. The exceptions are `Eq`, `Order`,
`Show` and `Hash`, which are ordinary interfaces over a value type (see
[Eq, Order, Show and Hash](eq-order-show-hash.md)).

## Guides

| Guide | Type classes |
|---|---|
| [Functor, Applicative, Monad](functor-applicative-monad.md) | `Invariant`, `Functor`, `Pointed`, `Apply`, `Applicative`, `ApplicativeOps`, `FlatMap`, `Monad`, `MonadOps`, `Alternative`, `MonadPlus`, `MonadZip`, `MonadRec`, `MonadFix`, `IxFunctor`, `IxPointed`, `IxApplicative`, `IxMonad`, `Copointed`, `Comonad` |
| [Selective](selective.md) | `Selective`, `MonadSelective` |
| [Foldable and Traversable](foldable-traversable.md) | `Foldable`, `Traversable`, `Foldable1`, `Traversable1`, `Bifoldable`, `Bitraversable` |
| [Align, Zip, Filterable and Witherable](align-filterable.md) | `Semialign`, `Align`, `Zip`, `Filterable`, `Witherable` |
| [Errors and MTL-style classes](mtl.md) | `ApplicativeError`, `MonadError`, `MonadReader`, `MonadState`, `MonadWriter`, `MonadTrans`, `MFunctor`, `MonadIO`, `MonadCont`, `MonadLogic`, lifted instances |
| [Eq, Order, Show and Hash](eq-order-show-hash.md) | `Eq`, `Order`, `Show`, `Hash` |
| [Contravariant functors](contravariant.md) | `Contravariant`, `Divisible`, `Decidable` |
| [Bifunctor, Profunctor and Arrow](bifunctor-profunctor-arrow.md) | `Bifunctor`, `Profunctor`, `Strong`, `Choice`, `Closed`, `Traversing`, `Wander`, `Cochoice`, `Costrong`, `Compose`, `Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `ArrowLoop` |
| [Distributive and Representable](distributive-representable.md) | `Distributive`, `Representable` |

`ComonadTrans`, `ComonadStore`, `ComonadEnv` and `ComonadTraced` are covered in [Comonads](../comonads.md);
`Adjunction` and `NaturalTransformation` in [Category theory](../category-theory.md); `Semigroup`, `Monoid`,
`CommutativeMonoid`, `Group`, `Band`, `Monus`, `Semiring`, `Ring`, `Field` and the lattices in
[Monoids and algebraic structures](../monoids.md).

## Hierarchy

<!-- nocheck -->
```text
Invariant ─┬─ Functor ─┬─ Apply ─┬─ Applicative (+ Pointed) ─┬─ Monad (+ FlatMap) ─┬─ MonadPlus (+ Alternative) ─ MonadLogic
           │           │         │                           │                     ├─ MonadZip, MonadRec, MonadFix, MonadOps
           │           │         │                           │                     ├─ MonadError (+ ApplicativeError)
           │           │         │                           │                     ├─ MonadReader, MonadState, MonadWriter
           │           │         │                           │                     ├─ MonadTrans, MonadIO, MonadCont
           │           │         │                           │                     └─ MonadSelective (+ Selective)
           │           │         │                           ├─ Selective
           │           │         │                           ├─ Alternative
           │           │         │                           └─ ApplicativeOps, ApplicativeError
           │           │         └─ FlatMap
           │           ├─ Traversable (+ Foldable) ─┬─ Traversable1 (+ Foldable1)
           │           │                            └─ Witherable (+ Filterable)
           │           ├─ Filterable
           │           ├─ Semialign ─┬─ Align
           │           │             └─ Zip
           │           ├─ Comonad (+ Copointed) ─ ComonadTrans, ComonadStore, ComonadEnv, ComonadTraced
           │           ├─ Distributive
           │           └─ Representable
           └─ Contravariant ─ Divisible ─ Decidable

Foldable ─ Foldable1
Bifunctor ─┐
Bifoldable ┴─ Bitraversable
MFunctor
IxFunctor ─ IxApplicative (+ IxPointed) ─ IxMonad
Profunctor ─┬─ Strong ─┐
            ├─ Choice ─┴─ Traversing (uses Wander)
            ├─ Closed
            ├─ Cochoice
            └─ Costrong
Compose ─ Category ─ Arrow ─┬─ ArrowChoice
                            ├─ ArrowApply
                            └─ ArrowLoop
Eq ─┬─ Order
    └─ Hash
Show
```

## Coverage

Instances provided for each data type (✓). `F`/`A`/`M` = Functor / Applicative / Monad, `Fo`/`Tr` = Foldable /
Traversable, `Co` = Comonad, `EOSH` = `Eq`, `Order`, `Show`, `Hash` factories. Types marked with * need a
`Class<M>` for a `Monoid` or `Semigroup` (see
[Higher-kinded types](../hkt.md#passing-monoids-and-semigroups-classm)).

### Core data types

See [Data types](../data-types.md).

| Data type | F | A | M | Fo | Tr | Co | EOSH | Other |
|---|---|---|---|---|---|---|---|---|
| `Identity` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | `Foldable1`, `Traversable1`, `MonadZip`, `MonadRec`, `MonadFix`, `Distributive`, `Representable<Unit>` |
| `Maybe` | ✓ | ✓ | ✓ | ✓ | ✓ | | ✓ | `Alternative`, `MonadPlus`, `MonadFix`, `Selective`, `Align`, `Zip`, `Witherable` |
| `Either<L, _>` | ✓ | ✓ | ✓ | ✓ | ✓ | | ✓ | `MonadError<L>`, `Selective`, `Bifunctor`, `Bitraversable`, `Witherable` (monoidal `L`) |
| `Try` | ✓ | ✓ | ✓ | ✓ | ✓ | | `Eq`, `Show`, `Hash` | `MonadError<Throwable>` |
| `Validated<E, _>` * | ✓ | ✓ | | | | | ✓ | `Selective`, `Bifunctor`, `Bitraversable`; error accumulation through `Semigroup<E>` |
| `Ior<A, _>` * | ✓ | ✓ | ✓ | | | | ✓ | `Bifunctor`, `Bitraversable` |
| `Eval` | ✓ | ✓ | ✓ | | | ✓ | | `MonadRec`, `MonadFix` |
| `Reader<R, _>` | ✓ | ✓ | ✓ | | | | | `MonadReader<R>`, `MonadFix`, `Distributive`, `Representable<R>` |
| `Writer<W, _>` * | ✓ | ✓ | ✓ | | | | | `MonadWriter<W>`, `Bifunctor` |
| `State<S, _>` | ✓ | ✓ | ✓ | | | | | `MonadState<S>`, `MonadRec` |
| `IndexedState<I, O, _>` | | | ✓ (`I = O`) | | | | | `IxFunctor`, `IxApplicative`, `IxMonad` |
| `RWS<R, W, S, _>` * | ✓ | ✓ | ✓ | | | | | `MonadReader`, `MonadWriter`, `MonadState` |
| `Cont<R, _>` | ✓ | ✓ | ✓ | | | | | `MonadCont` |
| `Select<R, _>` | ✓ | ✓ | ✓ | | | | | |
| `ListK` | ✓ | ✓ | ✓ | ✓ | ✓ | | ✓ | `Alternative`, `MonadPlus`, `MonadZip`, `Align`, `Zip`, `Witherable` |
| `NonEmptyList` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | `Foldable1`, `Traversable1`, `Semialign`, `Zip` |
| `Tuple2<S, _>` * | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | `Foldable1`, `Traversable1`, `Bifunctor`, `Bitraversable` |
| `Twin` | ✓ | | | | | | | `Representable<Boolean>` |
| `Const<C, _>` * | ✓ | ✓ | | | | | | `Contravariant`, `Selective` |
| `Function0` | ✓ | ✓ | ✓ | | | ✓ | | `MonadFix` |
| `Function1<R, _>` | ✓ | ✓ | ✓ | | | | | `Category`, `Arrow`, `Profunctor`, `Strong`, `Choice`, `Closed`, `Traversing`, `Cochoice` |
| `Kleisli<F, _, _>` | | | | | | | | `Category`, `Arrow`, `ArrowChoice`, `ArrowApply`, `ArrowLoop` (over `MonadFix`), `Profunctor`, `Strong`, `Choice` |
| `Predicate`, `Equivalence`, `Comparison` | | | | | | | | `Contravariant`, `Decidable` |
| `Op<R, _>` | | | | | | | | `Contravariant`, `Decidable` (needs `Monoid<R>`) |
| `MonoidOf`, `SemigroupOf` | | | | | | | | `Invariant` |
| `Compose<F, G, _>`, `Product<F, G, _>` | ✓ | ✓ | | ✓ | | | | |
| `Coproduct<F, G, _>` | ✓ | | | ✓ | | | | |

### Data structures, effects and automata

See [Data structures](../data-structures.md), [Effects](../effects.md) and [Automata](../automata.md).

| Data type | F | A | M | Fo | Tr | Co | Other |
|---|---|---|---|---|---|---|---|
| `LazyList` | ✓ | ✓ | ✓ | ✓ | ✓ | | `Alternative`, `Align`, `Zip`, `Witherable` |
| `Chain` | ✓ | ✓ | ✓ | ✓ | ✓ | | `Align`, `Zip`, `Witherable` |
| `Tree` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | `Foldable1`, `Traversable1`, `Semialign`, `Zip` |
| `NonEmptySet` | | | | ✓ | | | `Foldable1` |
| `NonEmptyMap<K, _>` | ✓ | | | ✓ | ✓ | | `Foldable1`, `Traversable1`, `Semialign` |
| `ListZipper` | ✓ | | | ✓ | ✓ | ✓ | `Foldable1`, `Traversable1` |
| `IO` | ✓ | ✓ | ✓ | | | | `MonadError<Throwable>`, `MonadIO` |
| `Resource` | ✓ | ✓ | ✓ | | | | |
| `Parser` | ✓ | ✓ | ✓ | | | | `Alternative`, `MonadPlus` |
| `Gen` | ✓ | ✓ | ✓ | | | | |
| `Moore<I, _>` | ✓ | ✓ | | | | ✓ | `Profunctor` |
| `Mealy<I, _>` | ✓ | | | | | | `Category`, `Arrow`, `ArrowChoice`, `Profunctor`, `Strong` |
| `Fold<A, _>` | ✓ | ✓ | | | | | `Profunctor` |
| `Cokleisli<W, _, _>` | | | | | | | `Category`, `Arrow`, `Profunctor` |

`Ref` has no instances; its operations return `IO`.

### Monad transformers

See [Monad transformers](../transformers.md) and, for the lifted MTL instances,
[Lifting through transformers](mtl.md#lifting-through-transformers). Every transformer is `Functor`,
`Applicative`, `Monad` and `MonadTrans`.

| Transformer | `MFunctor` | `MonadIO` | Other |
|---|---|---|---|
| `MaybeT`, `EitherT`, `ReaderT`, `StateT`, `WriterT` *, `IdentityT` | ✓ | ✓ | native and lifted `MonadReader` / `MonadState` / `MonadWriter` / `MonadError` |
| `RWST<F, R, W, S, _>` * | ✓ | ✓ | `MonadReader`, `MonadWriter`, `MonadState` |
| `IorT<F, A, _>` * | ✓ | ✓ | `MonadError<A>` |
| `AccumT<F, W, _>` * | ✓ | ✓ | |
| `ContT<R, F, _>` | | ✓ | `MonadCont` |
| `ListT<F, _>` | | ✓ | `Alternative`, `MonadPlus` |
| `LogicT<F, _>` | | ✓ | `Alternative`, `MonadPlus`, `MonadLogic` |
| `FreeT<F, M, _>` | ✓ | | |
| `SelectT<R, F, _>` | | | |
| `IndexedStateT<F, I, O, _>` | | | `IxFunctor`, `IxApplicative`, `IxMonad` (not a `MonadTrans`) |

### Comonads and category-theory constructs

See [Comonads](../comonads.md) and [Category theory](../category-theory.md).

| Data type | F | A | M | Co | Other |
|---|---|---|---|---|---|
| `Store`, `Env`, `Traced` * | ✓ | | | ✓ | `ComonadStore` / `ComonadEnv` / `ComonadTraced` respectively |
| `StoreT`, `EnvT`, `TracedT` * | ✓ | | | ✓ | `ComonadTrans`, plus `ComonadStore` / `ComonadEnv` / `ComonadTraced` |
| `Cofree`, `Density` | ✓ | | | ✓ | |
| `CofreeT` | ✓ | | | ✓ | `ComonadTrans` |
| `Day<F, G, _>` | ✓ | ✓ | | ✓ | |
| `Free`, `Codensity`, `Freer` | ✓ | ✓ | ✓ | | |
| `FreeAp` | ✓ | ✓ | | | |
| `FreeSelective` | ✓ | ✓ | | | `Selective` |
| `FreeCategory`, `FreeArrow` | | | | | `Category` / `Arrow` |
| `Yoneda`, `Coyoneda`, `Ran`, `Lan` | ✓ | | | | |
| `Star`, `Costar`, `Forget`, `Tagged` | | | | | profunctor classes, see [Bifunctor, Profunctor and Arrow](bifunctor-profunctor-arrow.md#profunctor-data-types) |
| `Procompose`, `Cayley`, `Tambara` | | | | | `Profunctor`, `Strong` |

`Fix`, `Mu` and `Nu` (fixed points for recursion schemes) have no instances; see
[Category theory](../category-theory.md). Profunctor optics (`Iso`, `Lens`, `Prism`, `Optional`, `Traversal`,
`Getter`, `Fold`, `Setter`) are described in [Optics](../optics.md).

# Kotlinz guides

These guides explain how Kotlinz is put together and how to use it. Every Kotlin sample compiles against the current
API and its `check(...)` assertions hold. New to Kotlinz? Start with [Getting started](getting-started.md). The
guides are also published on the [project website](https://kotlinz.github.io/kotlinz/), next to the generated
[API reference](https://kotlinz.github.io/kotlinz/api/).

## Reading order

1. **[Higher-kinded types and conventions](hkt.md)**: the `K1` / `T` encoding, `narrow`, instance interfaces vs.
   member and companion helpers, how monoids are passed as `Class<M>`, and instance values for `Eq` / `Order` /
   `Show` / `Hash`. Read this first; everything else builds on it.
2. **[Type classes](type-classes/README.md)**: the hierarchy and a coverage matrix of which data type implements
   which class.
   - [Functor, Applicative, Monad (and Comonad)](type-classes/functor-applicative-monad.md), including `Apply`,
     `FlatMap` and indexed monads
   - [Selective applicative functors](type-classes/selective.md)
   - [Foldable and Traversable](type-classes/foldable-traversable.md), including `Foldable1`, `Traversable1`,
     `Bifoldable` and `Bitraversable`
   - [Align and Filterable](type-classes/align-filterable.md): `Semialign`, `Align`, `Zip`, `Filterable`,
     `Witherable`
   - [Errors and MTL-style classes](type-classes/mtl.md): `MonadError`, `MonadReader`, `MonadState`,
     `MonadWriter`, `MonadCont`, `MonadIO`, `MonadTrans`, `MFunctor` and lifted instances
   - [Eq, Order, Show and Hash](type-classes/eq-order-show-hash.md)
   - [Contravariant functors](type-classes/contravariant.md): `Contravariant`, `Divisible`, `Decidable`
   - [Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md)
   - [Distributive and Representable](type-classes/distributive-representable.md)
3. **[Data types](data-types.md)**: `Identity`, `Maybe`, `Either`, `Try`, `Validated`, `Ior`, `Eval`, `IO`,
   `Reader`, `Writer`, `State`, `IndexedState`, `RWS`, `Cont`, `Select`, `ListK`, `NonEmptyList`, `Tuple2`,
   `Twin`, `Const`, functions and functor combinators.
4. **[Data structures](data-structures.md)**: `LazyList`, `Chain`, `Tree`, `NonEmptySet`, `NonEmptyMap`,
   `ListZipper`.
5. **[Effects](effects.md)**: `IO`, `Resource`, `Ref`, `Parser`, `Gen`, `Logic`.
6. **[Monad comprehension (`fx`)](fx.md)**: do-notation with coroutines, `fxReplay`, and caveats.
7. **[Monad transformers](transformers.md)**: `MaybeT`, `EitherT`, `IorT`, `IdentityT`, `ReaderT`, `WriterT`,
   `StateT`, `IndexedStateT`, `RWST`, `AccumT`, `ContT`, `SelectT`, `ListT`, `LogicT`, `FreeT`.
8. **[Monoids and algebraic structures](monoids.md)**: monoids, `CommutativeMonoid`, `Group`, `Band`, `Monus`,
   `Semiring`, `Ring`, `Field`, lattices, `MonoidOf` / `SemigroupOf`.
9. **[Optics](optics.md)**: `Iso`, `Lens`, `Prism`, `Optional`, `Traversal`, `Getter`, `Fold`, `Setter`.
10. **[Comonads](comonads.md)**: `Store`, `Env`, `Traced`, `Cofree`, `Density` and the comonad transformers.
11. **[Automata and folds](automata.md)**: `Moore`, `Mealy`, `Fold`, `Cokleisli`.
12. **[Category theory](category-theory.md)**: natural transformations, adjunctions, Yoneda / Coyoneda,
    Codensity, Kan extensions, Day convolution, `Free` / `FreeAp` / `Freer` / `FreeSelective`, `FreeCategory` /
    `FreeArrow`, and recursion schemes over `Fix`, `Mu` and `Nu`.

## Known limitations at a glance

- `narrow` is an unchecked cast, and `K1` is invariant, so type arguments sometimes need to be spelled out
  ([details](hkt.md#known-limitations-of-the-encoding)).
- Some `<DataType>Applicative` interfaces do not extend the matching functor; use the `Monad` instance where an
  `Applicative` is required ([details](hkt.md#instance-interfaces-vs-companion-and-member-helpers)).
- Generic monoid classes (`FirstMonoid<A>`, `MaxMonoid<A>`, `EndoMonoid<A>`, ...) need an unchecked
  `Class` cast ([details](monoids.md#caveats)).
- `Alternative` omits `some` / `many`, which do not terminate under strict evaluation.
- `ArrowLoop` feedback is lazy: `loop` takes an arrow over `Pair<A, () -> D>`, and its only instance is `Kleisli`
  over a `MonadFix` ([details](type-classes/bifunctor-profunctor-arrow.md)).
- Recursion schemes and long `fx` blocks are not stack-safe; `MonadRec` (`Identity`, `Eval`, `State`) and
  `Free.foldMap` into a `MonadRec` are.
- `IO` and `Eval` `bind` chains are not trampolined.

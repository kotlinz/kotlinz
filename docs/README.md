# Kotlinz guides

These guides explain how Kotlinz is put together and how to use it. Every Kotlin sample compiles against the current
API and its `check(...)` assertions hold. New to Kotlinz? Start with [Getting started](getting-started.md). The
guides are also published on the [project website](https://kotlinz.github.io/kotlinz/), next to the generated
[API reference](https://kotlinz.github.io/kotlinz/api/).

## Reading order

1. **[Higher-kinded types and conventions](hkt.md)**: the `K1` / `T` encoding, `narrow`, instance interfaces vs.
   member and companion helpers, and how monoids are passed as `Class<M>`. Read this first; everything else
   builds on it.
2. **[Type classes](type-classes/README.md)**: the hierarchy and a coverage matrix of which data type implements
   which class.
   - [Functor, Applicative, Monad (and Comonad)](type-classes/functor-applicative-monad.md)
   - [Foldable and Traversable](type-classes/foldable-traversable.md)
   - [Errors and MTL-style classes](type-classes/mtl.md): `MonadError`, `MonadReader`, `MonadState`, `MonadWriter`
   - [Contravariant functors](type-classes/contravariant.md): `Contravariant`, `Divisible`, `Decidable`
   - [Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md)
   - [Distributive and Representable](type-classes/distributive-representable.md)
3. **[Data types](data-types.md)**: `Identity`, `Maybe`, `Either`, `Try`, `Validated`, `Ior`, `Eval`, `IO`,
   `Reader`, `Writer`, `State`, `RWS`, `Cont`, `ListK`, `NonEmptyList`, `Tuple2`, `Twin`, `Const`, functions and
   functor combinators.
4. **[Monad comprehension (`fx`)](fx.md)**: do-notation with coroutines, `fxReplay`, and caveats.
5. **[Monad transformers](transformers.md)**: `MaybeT`, `EitherT`, `ReaderT`, `StateT`, `WriterT`.
6. **[Monoids and algebraic structures](monoids.md)**: monoids, `Group`, `Band`, `Semiring`, lattices,
   `MonoidOf` / `SemigroupOf`.
7. **[Comonads](comonads.md)**: `Store`, `Env`, `Traced`, `Cofree`, `Density`.
8. **[Category theory](category-theory.md)**: natural transformations, adjunctions, Yoneda / Coyoneda, Codensity,
   Kan extensions, Day convolution, `Free` / `FreeAp`, `Fix` and recursion schemes.

## Known limitations at a glance

- `narrow` is an unchecked cast, and `K1` is invariant, so type arguments sometimes need to be spelled out
  ([details](hkt.md#known-limitations-of-the-encoding)).
- Some `<DataType>Applicative` interfaces do not extend the matching functor; use the `Monad` instance where an
  `Applicative` is required ([details](hkt.md#instance-interfaces-vs-companion-and-member-helpers)).
- Generic monoid classes (`FirstMonoid<A>`, `MaxMonoid<A>`, `EndoMonoid<A>`, ...) need an unchecked
  `Class` cast ([details](monoids.md#caveats)).
- `Alternative` omits `some` / `many`, which do not terminate under strict evaluation.
- `ArrowLoop` is defined but has no instances.
- Recursion schemes and long `fx` blocks are not stack-safe; `MonadRec` (`Identity`, `Eval`, `State`) and
  `Free.foldMap` into a `MonadRec` are.
- `IO` and `Eval` `bind` chains are not trampolined.

# Monoids and algebraic structures

Algebraic structures live in `com.github.kotlinz.kotlinz.type.group`, `type.ring` and `type.lattice`; the instances
are in `data.monoid`, `data.semiring`, `data.lattice`, `data.monoidof` and `data.semigroupof`.

## Encoding

Structures are F-bounded interfaces implemented by small wrapper classes. The operations take `Self`, and each
wrapper has a public no-argument constructor so that generic code can obtain an instance from a `Class`
(see [Higher-kinded types](hkt.md#passing-monoids-and-semigroups-classm)):

<!-- nocheck -->
```kotlin
interface Semigroup<A, Self: Semigroup<A, Self>> { fun op(a: Self, b: Self): Self }
interface Monoid<A, Self: Monoid<A, Self>>: Semigroup<A, Self> { fun mzero(): Self }
interface Group<A, Self: Group<A, Self>>: Monoid<A, Self> { fun inverse(a: Self): Self }
interface Band<A, Self: Band<A, Self>>: Semigroup<A, Self>   // op(a, a) == a
interface CommutativeMonoid<A, Self: CommutativeMonoid<A, Self>>: Monoid<A, Self>   // op(a, b) == op(b, a)
interface Monus<A, Self: Monus<A, Self>>: CommutativeMonoid<A, Self> { fun monus(a: Self, b: Self): Self }
```

Each interface has companion helpers that take the class: `Semigroup.op(type, a, b)`, `Monoid.mzero(type)`,
`Group.inverse(type, a)`, `Monus.monus(type, a, b)`, and likewise for `Semiring`, `Ring`, `Field` and the lattices.

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.monoid.toSumMonoid
import com.github.kotlinz.kotlinz.type.group.Group
import com.github.kotlinz.kotlinz.type.group.Monoid

val sum = SumMonoid::class.java
check(Monoid.op(sum, 1.toSumMonoid(), 2.toSumMonoid()) == SumMonoid(3))
check(Monoid.mzero(sum) == SumMonoid(0))
check(Group.inverse(sum, SumMonoid(5)) == SumMonoid(-5))
check(SumMonoid().op(SumMonoid(1), SumMonoid(2)) == SumMonoid(3))
```

### Value-level structures vs. instance-style type classes

The algebraic structures on this page are *value-level*: the wrapper value itself (`SumMonoid(3)`) carries the
operations, and generic code receives a `Class<M>`. The comparison classes `Eq`, `Order`, `Show` and `Hash` work the
other way round: they are ordinary instance interfaces over a plain type (`Eq<Int>`, `Order<List<A>>`), passed as
values just like `Functor` instances, with data-type instances such as `MaybeEq` or `EitherOrder`. See
[Eq, Order, Show and Hash](type-classes/eq-order-show-hash.md). If you need a monoid as a plain value, use
[`MonoidOf`](#first-class-dictionaries-monoidof-and-semigroupof).

## Provided monoids

| Class | Wraps | `op` | identity | Also | Constructor helper |
|---|---|---|---|---|---|
| `SumMonoid` / `LongSumMonoid` | `Int` / `Long` | `+` | `0` | `Group`, `CommutativeMonoid` | `toSumMonoid()` |
| `ProductMonoid` / `LongProductMonoid` | `Int` / `Long` | `*` | `1` | `CommutativeMonoid` | `toProductMonoid()` |
| `NaturalMonoid` | `Long` (non-negative) | `+` (throws on overflow) | `0` | `Monus` (truncated subtraction) | `toNaturalMonoid()` |
| `XorMonoid` | `Boolean` | `xor` | `false` | `Group`, `CommutativeMonoid` | `toXorMonoid()` |
| `AllMonoid` | `Boolean` | `&&` | `true` | `Band`, `CommutativeMonoid` | `toAllMonoid()` |
| `AnyMonoid` | `Boolean` | `\|\|` | `false` | `Band`, `CommutativeMonoid` | `toAnyMonoid()` |
| `SetMonoid<A>` | `Set<A>` | union | `{}` | `Band`, `Monus` (set difference) | `toSetMonoid()` |
| `MultisetMonoid<A>` | `Map<A, Int>` (counts) | adds multiplicities | `{}` | `Monus` (multiset difference) | `toMultisetMonoid()` (from a `List`) |
| `StringMonoid` | `String` | concatenation | `""` | | `toStringMonoid()` |
| `ListMonoid` | `List<*>` | concatenation | `[]` | | `toListMonoid()` |
| `FirstMonoid<A>` / `LastMonoid<A>` | `Maybe<A>` | first / last `Just` | `None` | `Band` | `toFirstMonoid()` / `toLastMonoid()` |
| `MaxMonoid<A>` / `MinMonoid<A>` | `Maybe<A: Comparable>` | max / min | `None` | `Band` | `toMaxMonoid()` / `toMinMonoid()` |
| `DualMonoid<M>` | `Maybe<M>` | `M`'s `op` with flipped arguments | `None` | | `toDualMonoid()` |
| `EndoMonoid<A>` | `(A) -> A` | function composition | identity function | | `toEndoMonoid()` |

```kotlin
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.AllMonoid
import com.github.kotlinz.kotlinz.data.monoid.DualMonoid
import com.github.kotlinz.kotlinz.data.monoid.EndoMonoid
import com.github.kotlinz.kotlinz.data.monoid.FirstMonoid
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toAllMonoid
import com.github.kotlinz.kotlinz.data.monoid.toDualMonoid
import com.github.kotlinz.kotlinz.data.monoid.toEndoMonoid
import com.github.kotlinz.kotlinz.data.monoid.toFirstMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid

val words = listKOf("a", "b", "c")

check(words.foldMap(AllMonoid::class.java) { it.isNotEmpty().toAllMonoid() } == AllMonoid(true))

@Suppress("UNCHECKED_CAST")
val first = FirstMonoid::class.java as Class<FirstMonoid<String>>
check(words.foldMap(first) { Maybe.Just(it).toFirstMonoid() } == FirstMonoid(Maybe.Just("a")))

@Suppress("UNCHECKED_CAST")
val dual = DualMonoid::class.java as Class<DualMonoid<StringMonoid>>
check(words.foldMap(dual) { it.toStringMonoid().toDualMonoid() } == DualMonoid(Maybe.Just(StringMonoid("cba"))))

@Suppress("UNCHECKED_CAST")
val endo = EndoMonoid::class.java as Class<EndoMonoid<Int>>
val pipeline = listKOf<(Int) -> Int>({ it + 1 }, { it * 10 }).foldMap(endo) { it.toEndoMonoid() }
check(pipeline(2) == 21)
```

Note that `EndoMonoid` composes right to left (`op(f, g)` is `f ∘ g`), just like Haskell's `Endo`.

## Caveats

- **Unchecked casts.** `Foo::class.java` for a generic class `Foo<A>` is a raw class, so `FirstMonoid`,
  `LastMonoid`, `MaxMonoid`, `MinMonoid`, `DualMonoid`, `EndoMonoid`, `SetMonoid` and `MultisetMonoid` need `as Class<Foo<X>>` with
  `@Suppress("UNCHECKED_CAST")`.
- **`ListMonoid` is untyped.** It stores a `List<*>`, so reading the elements back requires a cast.
- **Reflection.** `Monoid.op(type, ...)` instantiates `type` through its no-argument constructor every time. Your own
  instances must have one (a secondary `constructor(): this(identity)` as in the built-in classes).
- **`DualMonoid` / `MaxMonoid` / `MinMonoid` wrap `Maybe`** so that an identity exists for any `M` / `A`.
- Laws are not enforced by the types; the test suite checks them for the built-in instances.

## Commutative monoids and Monus

`CommutativeMonoid` is a marker for monoids whose `op` is commutative, so elements may be combined in any order
(folding unordered collections, merging partial results). It adds no operations. `Monus` extends it with
*truncated subtraction*: `monus(a, b)` removes as much of `b` from `a` as possible without going below `mzero()`.
Instances: `NaturalMonoid` (`5 ∸ 3 == 2`, `3 ∸ 5 == 0`), `MultisetMonoid<A>` and `SetMonoid<A>` (set difference).

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.MultisetMonoid
import com.github.kotlinz.kotlinz.data.monoid.NaturalMonoid
import com.github.kotlinz.kotlinz.data.monoid.SetMonoid
import com.github.kotlinz.kotlinz.data.monoid.toMultisetMonoid
import com.github.kotlinz.kotlinz.data.monoid.toNaturalMonoid
import com.github.kotlinz.kotlinz.data.monoid.toSetMonoid
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Monus

val naturals = NaturalMonoid::class.java
check(Monus.monus(naturals, 5L.toNaturalMonoid(), 3L.toNaturalMonoid()) == NaturalMonoid(2))
check(Monus.monus(naturals, 3L.toNaturalMonoid(), 5L.toNaturalMonoid()) == NaturalMonoid(0))

@Suppress("UNCHECKED_CAST")
val bags = MultisetMonoid::class.java as Class<MultisetMonoid<String>>
val stock = listOf("apple", "apple", "pear").toMultisetMonoid()
val sold = listOf("apple", "plum").toMultisetMonoid()
check(Monus.monus(bags, stock, sold) == MultisetMonoid(mapOf("apple" to 1, "pear" to 1)))
check(Monoid.op(bags, stock, sold).count("apple") == 3)

@Suppress("UNCHECKED_CAST")
val sets = SetMonoid::class.java as Class<SetMonoid<Int>>
check(Monus.monus(sets, setOf(1, 2, 3).toSetMonoid(), setOf(2).toSetMonoid()) == SetMonoid(setOf(1, 3)))
```

`NaturalMonoid` and `MultisetMonoid` validate their contents: constructing them with a negative value or a
non-positive count throws `IllegalArgumentException`, and `NaturalMonoid` addition throws `ArithmeticException` on
overflow instead of wrapping.

## Semiring, Ring and Field

`Semiring<A, Self>` has `plus`, `times`, `zero` and `one`. `Ring` adds additive inverses (`negate`, `minus`), and
`Field` adds multiplicative inverses (`reciprocal`, `div`). Instances: `IntSemiring` and `LongSemiring` (arithmetic,
both `Ring`s despite the name) and `BooleanSemiring` (`||` / `&&`), built with `toSemiring()`; and `DoubleField`,
built with `toField()`.

```kotlin
import com.github.kotlinz.kotlinz.data.semiring.BooleanSemiring
import com.github.kotlinz.kotlinz.data.semiring.IntSemiring
import com.github.kotlinz.kotlinz.data.semiring.toSemiring
import com.github.kotlinz.kotlinz.type.ring.Semiring

val ints = IntSemiring::class.java
check(Semiring.times(ints, Semiring.plus(ints, 2.toSemiring(), 3.toSemiring()), 4.toSemiring()) == IntSemiring(20))
check(Semiring.one(BooleanSemiring::class.java) == BooleanSemiring(true))
```

```kotlin
import com.github.kotlinz.kotlinz.data.semiring.DoubleField
import com.github.kotlinz.kotlinz.data.semiring.IntSemiring
import com.github.kotlinz.kotlinz.data.semiring.toField
import com.github.kotlinz.kotlinz.data.semiring.toSemiring
import com.github.kotlinz.kotlinz.type.ring.Field
import com.github.kotlinz.kotlinz.type.ring.Ring

check(Ring.minus(IntSemiring::class.java, 3.toSemiring(), 5.toSemiring()) == IntSemiring(-2))
check(Ring.negate(IntSemiring::class.java, 7.toSemiring()) == IntSemiring(-7))

val doubles = DoubleField::class.java
check(Field.div(doubles, 3.0.toField(), 2.0.toField()) == DoubleField(1.5))
check(Field.reciprocal(doubles, 4.0.toField()) == DoubleField(0.25))
```

`DoubleField` follows IEEE 754: the laws hold only approximately because of rounding, `NaN` breaks them, and
`reciprocal(zero())` is `Infinity`. Equality is that of boxed doubles, so `-0.0` and `0.0` differ.

## Lattices

`JoinSemilattice` (`join`), `MeetSemilattice` (`meet`), `Lattice` (both) and `BoundedLattice` (`top`, `bottom`).
Instances: `BooleanLattice` (bounded; `||` / `&&`) and `SetLattice<A>` (union / intersection, not bounded), built with
`toLattice()`.

```kotlin
import com.github.kotlinz.kotlinz.data.lattice.BooleanLattice
import com.github.kotlinz.kotlinz.data.lattice.SetLattice
import com.github.kotlinz.kotlinz.data.lattice.toLattice
import com.github.kotlinz.kotlinz.type.lattice.BoundedLattice

val a = setOf(1, 2).toLattice()
val b = setOf(2, 3).toLattice()
check(a.join(a, b) == SetLattice(setOf(1, 2, 3)))
check(a.meet(a, b) == SetLattice(setOf(2)))
check(BoundedLattice.top(BooleanLattice::class.java) == BooleanLattice(true))
```

## First-class dictionaries: MonoidOf and SemigroupOf

When a structure should be a value rather than a class (for example to build one at runtime, or to transport it
along an isomorphism), use `MonoidOf<A>(empty, combine)` and `SemigroupOf<A>(combine)`. They can be created from a
class with `from`, and they are `Invariant` functors (`imap`):

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.data.semigroupof.SemigroupOf

val sumOfInts: MonoidOf<Int> = MonoidOf.from(SumMonoid::class.java).imap({ it.value }, { SumMonoid(it) })
check(listOf(1, 2, 3).fold(sumOfInts.empty, sumOfInts.combine) == 6)

val longest = SemigroupOf { a: String, b: String -> if (b.length > a.length) b else a }
check(listOf("a", "abc", "ab").reduce(longest.combine) == "abc")
```

`MonoidOf` / `SemigroupOf` are not accepted where a `Class<M>` is expected (`Writer`, `foldMap`, ...); those APIs
only take the class-based encoding.

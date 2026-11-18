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
```

Each interface has companion helpers that take the class: `Semigroup.op(type, a, b)`, `Monoid.mzero(type)`,
`Group.inverse(type, a)`, and likewise for `Semiring` and the lattices.

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

## Provided monoids

| Class | Wraps | `op` | identity | Also | Constructor helper |
|---|---|---|---|---|---|
| `SumMonoid` / `LongSumMonoid` | `Int` / `Long` | `+` | `0` | `Group` | `toSumMonoid()` |
| `ProductMonoid` / `LongProductMonoid` | `Int` / `Long` | `*` | `1` | | `toProductMonoid()` |
| `XorMonoid` | `Boolean` | `xor` | `false` | `Group` | `toXorMonoid()` |
| `AllMonoid` | `Boolean` | `&&` | `true` | `Band` | `toAllMonoid()` |
| `AnyMonoid` | `Boolean` | `\|\|` | `false` | `Band` | `toAnyMonoid()` |
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
  `LastMonoid`, `MaxMonoid`, `MinMonoid`, `DualMonoid` and `EndoMonoid` need `as Class<Foo<X>>` with
  `@Suppress("UNCHECKED_CAST")`.
- **`ListMonoid` is untyped.** It stores a `List<*>`, so reading the elements back requires a cast.
- **Reflection.** `Monoid.op(type, ...)` instantiates `type` through its no-argument constructor every time. Your own
  instances must have one (a secondary `constructor(): this(identity)` as in the built-in classes).
- **`DualMonoid` / `MaxMonoid` / `MinMonoid` wrap `Maybe`** so that an identity exists for any `M` / `A`.
- Laws are not enforced by the types; the test suite checks them for the built-in instances.

## Semiring

`Semiring<A, Self>` has `plus`, `times`, `zero` and `one`. Instances: `IntSemiring`, `LongSemiring`
(arithmetic) and `BooleanSemiring` (`||` / `&&`), built with `toSemiring()`.

```kotlin
import com.github.kotlinz.kotlinz.data.semiring.BooleanSemiring
import com.github.kotlinz.kotlinz.data.semiring.IntSemiring
import com.github.kotlinz.kotlinz.data.semiring.toSemiring
import com.github.kotlinz.kotlinz.type.ring.Semiring

val ints = IntSemiring::class.java
check(Semiring.times(ints, Semiring.plus(ints, 2.toSemiring(), 3.toSemiring()), 4.toSemiring()) == IntSemiring(20))
check(Semiring.one(BooleanSemiring::class.java) == BooleanSemiring(true))
```

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

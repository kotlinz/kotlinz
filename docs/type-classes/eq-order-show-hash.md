# Eq, Order, Show and Hash

`Eq`, `Order`, `Show` and `Hash` (in `type.eq`, `type.order`, `type.show`, `type.hash`) describe equality,
ordering, rendering and hashing of *plain values*. Unlike the other type classes they are not higher-kinded: they
are interfaces over a value type `A`, and you pass instances around as ordinary values.

<!-- nocheck -->
```kotlin
interface Eq<A> { fun eqv(a: A, b: A): Boolean; fun neqv(a: A, b: A): Boolean }
interface Order<A>: Eq<A> {
  fun compare(a: A, b: A): Int
  // lt, lteqv, gt, gteqv, min, max, toComparator() are derived; eqv is compare(a, b) == 0
}
interface Show<A> { fun show(a: A): String }
interface Hash<A>: Eq<A> { fun hash(a: A): Int }
```

## Instance-style vs. value-level classes

Kotlinz has two styles of "first-order" type class, and it helps to know which one you are dealing with:

| Style | Classes | How you get an instance | How you pass it |
|---|---|---|---|
| **Instance values** | `Eq`, `Order`, `Show`, `Hash` | companion extensions (`Int.eq`, `Maybe.order(...)`) or factories (`Eq.by { ... }`) | as a value of type `Eq<A>` |
| **Self-typed wrappers** | `Semigroup`, `Monoid`, `Group`, `Semiring`, ... | wrap the value (`SumMonoid(1)`) | as a `Class<M>`, instantiated reflectively (see [Higher-kinded types](../hkt.md#passing-monoids-and-semigroups-classm)) |
| **Monoid values** | `MonoidOf`, `SemigroupOf` | `MonoidOf(empty, combine)` | as a value (see [Monoids](../monoids.md#first-class-dictionaries-monoidof-and-semigroupof)) |

`Eq` and friends are the simple style: there is no wrapper type and no reflection, so you can build an instance for
any type on the spot, including from a lambda.

## Primitive instances and factories

Primitive types get instances as companion extension properties: `Int.eq`, `String.order`, `Double.show`,
`Char.hash`, ... They are imported from the type-class package (`type.eq.eq`, `type.order.order`, ...).

```kotlin
import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.eq.eq
import com.github.kotlinz.kotlinz.type.hash.Hash
import com.github.kotlinz.kotlinz.type.hash.hash
import com.github.kotlinz.kotlinz.type.order.Order
import com.github.kotlinz.kotlinz.type.order.order
import com.github.kotlinz.kotlinz.type.show.Show
import com.github.kotlinz.kotlinz.type.show.show

check(Int.eq.eqv(1, 1))
check(Int.order.lt(1, 2))
check(String.order.max("apple", "pear") == "pear")
check(String.show.show("hi") == "\"hi\"")
check(Char.show.show('a') == "'a'")
check(Int.hash.hash(42) == 42.hashCode())

val ignoreCase = Eq.by { a: String, b: String -> a.equals(b, ignoreCase = true) }
val byLength = Order.by { a: String, b: String -> a.length.compareTo(b.length) }
check(ignoreCase.eqv("Kotlin", "KOTLIN"))
check(byLength.eqv("abc", "xyz"))
check(listOf("ccc", "a", "bb").sortedWith(byLength.toComparator()) == listOf("a", "bb", "ccc"))
check(Show.by { n: Int -> "#$n" }.show(7) == "#7")

check(Eq.list(ignoreCase).eqv(listOf("A", "b"), listOf("a", "B")))
check(Order.list(Int.order).lt(listOf(1, 2), listOf(1, 3)))
check(Show.list(Int.show).show(listOf(1, 2)) == "[1, 2]")
check(Hash.list(Int.hash).hash(listOf(1, 2)) == Hash.combine(1, 2))
```

| Factory | Description |
|---|---|
| `Eq.fromEquals()` / `EqualsEq` | uses `==` |
| `Order.fromComparable()` / `ComparableOrder` | uses `compareTo` |
| `Order.fromComparator(c)` | wraps a `java.util.Comparator` |
| `Show.fromToString()` / `ToStringShow` | uses `toString()` |
| `Hash.fromHashCode()` / `HashCodeHash` | uses `==` and `hashCode()` |
| `Eq.by`, `Order.by`, `Show.by`, `Hash.by(eq, f)` | build an instance from a function |
| `Eq.list`, `Order.list` (lexicographic), `Show.list`, `Hash.list` | lift an element instance to `List<A>` |
| `Hash.combine(vararg Int)` | combine hashes with the usual `31 * acc + h` scheme |

Floating-point instances follow the boxed JVM semantics so that `Eq`, `Order` and `Hash` agree with each other:
`Double.NaN` equals itself and is the largest value, and `0.0` and `-0.0` are different (`-0.0 < 0.0`).

## Data type instances

`Identity`, `Maybe`, `Either`, `Try`, `Validated`, `Ior`, `ListK`, `NonEmptyList` and `Tuple2` derive instances from
the instances of their elements. The factories are extension functions on the companions, defined next to the
instance interfaces (`MaybeEq`, `EitherOrder`, `ListKShow`, `Tuple2Hash`, ...), so they need an import from the data
type's package, for example `import com.github.kotlinz.kotlinz.data.maybe.eq`.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.order
import com.github.kotlinz.kotlinz.data.either.show
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.listk.show
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.eq
import com.github.kotlinz.kotlinz.data.maybe.hash
import com.github.kotlinz.kotlinz.data.maybe.order
import com.github.kotlinz.kotlinz.data.maybe.show
import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.hash
import com.github.kotlinz.kotlinz.type.order.order
import com.github.kotlinz.kotlinz.type.show.show

val ignoreCase = Eq.by { a: String, b: String -> a.equals(b, ignoreCase = true) }
check(Maybe.eq(ignoreCase).eqv(Maybe.Just("A"), Maybe.Just("a")))
check(Maybe.eq(ignoreCase).neqv(Maybe.Just("A"), Maybe.None()))

val maybeOrder = Maybe.order(Int.order)
check(maybeOrder.lt(Maybe.None(), Maybe.Just(0)))
check(listOf(Maybe.Just(2), Maybe.None(), Maybe.Just(1)).sortedWith(maybeOrder.toComparator()) ==
  listOf(Maybe.None(), Maybe.Just(1), Maybe.Just(2)))

val eitherOrder = Either.order(String.order, Int.order)
check(eitherOrder.lt(Either.Left("z"), Either.Right(0)))

check(Maybe.show(String.show).show(Maybe.Just("a")) == "Just(\"a\")")
check(Either.show(String.show, Int.show).show(Either.Left("e")) == "Left(\"e\")")
check(ListK.show(Int.show).show(listKOf(1, 2)) == "ListK([1, 2])")

check(Maybe.hash(Int.hash).hash(Maybe.None()) == 0)
check(Maybe.hash(Int.hash).eqv(Maybe.Just(1), Maybe.Just(1)))
```

| Data type | `Eq` | `Order` | `Show` | `Hash` |
|---|---|---|---|---|
| `Identity`, `Maybe`, `ListK`, `NonEmptyList` | `X.eq(eqA)` | `X.order(orderA)` | `X.show(showA)` | `X.hash(hashA)` |
| `Either`, `Validated`, `Ior`, `Tuple2` | `X.eq(eqL, eqR)` | `X.order(...)` | `X.show(...)` | `X.hash(...)` |
| `Try` | `Try.eq(eqA, eqError = Eq.fromEquals())` | – | `Try.show(showA, showError = ...)` | `Try.hash(hashA, hashError = ...)` |

Orderings put the "smaller" constructor first: `None < Just`, `Left < Right`, `Invalid < Valid`,
`Left < Right < Both` for `Ior`; lists and `NonEmptyList` compare lexicographically. `Try` has no `Order`, because
exceptions have no natural ordering, and compares failures with `equals` (reference identity for most exceptions)
unless you pass your own `Eq<Throwable>`. Each `<Type>Hash` extends the matching `<Type>Eq` and uses the element
`Hash` instances for both, so `eqv` and `hash` always agree.

The core data types also override `equals`, `hashCode` and `toString` structurally, so `==` and string templates
work without these instances. Use the type classes when you need a *different* notion (case-insensitive equality,
a custom ordering, a quoted rendering) or when writing generic code that should not rely on `equals`.

## Relation to `Equivalence` and `Comparison`

[`Equivalence<A>` and `Comparison<A>`](contravariant.md) are data types wrapping `(A, A) -> Boolean` and
`(A, A) -> Int`. They exist to be `Contravariant` / `Decidable` (`contramap`, `divide`, `choose`), whereas `Eq` and
`Order` are the instances you pass to generic code. Converting between them is a one-liner:

```kotlin
import com.github.kotlinz.kotlinz.data.comparison.Comparison
import com.github.kotlinz.kotlinz.type.order.Order
import com.github.kotlinz.kotlinz.type.order.order

data class User(val name: String, val age: Int)

val byAge = Comparison.natural<Int>() contramap { u: User -> u.age }
val order: Order<User> = Order.fromComparator(byAge.toComparator())
check(order.gt(User("old", 90), User("young", 9)))
check(Order.by(byAge.compare).eqv(User("a", 1), User("b", 1)))
check(Order.by { a: User, b: User -> Int.order.compare(a.age, b.age) }.max(User("a", 1), User("b", 2)) == User("b", 2))
```

`MinMonoid`, `MaxMonoid` and the lattices still rely on `Comparable`, because they are instantiated reflectively
and cannot receive an `Order` value. Bridge the two with `Order.fromComparable()` and `toComparator()`.

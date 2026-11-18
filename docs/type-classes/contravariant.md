# Contravariant functors

A *contravariant* functor consumes values instead of producing them, so mapping goes the other way: to turn a
`Predicate<A>` into a `Predicate<B>` you need a function `(B) -> A`.

<!-- nocheck -->
```kotlin
interface Contravariant<T>: Invariant<T> { fun <A, B> contramap(f: (B) -> A, v: K1<T, A>): K1<T, B> }
interface Divisible<T>: Contravariant<T> {
  fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  fun <A> conquer(): K1<T, A>
}
interface Decidable<T>: Divisible<T> {
  fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  fun <A> lose(f: (A) -> Nothing): K1<T, A>
}
```

`Divisible` is the contravariant analogue of `Applicative` (split the input and consume both halves), and
`Decidable` the analogue of `Alternative` (pick one of two consumers).

| Data type | Wraps | `Contravariant` | `Decidable` (`divide` / `choose`) |
|---|---|---|---|
| `Predicate<A>` | `(A) -> Boolean` | ✓ | both parts must hold / test the chosen branch |
| `Equivalence<A>` | `(A, A) -> Boolean` | ✓ | equivalent on both parts / same branch and equivalent |
| `Comparison<A>` | `(A, A) -> Int` | ✓ | lexicographic / every `Left` before every `Right` |
| `Op<R, A>` | `(A) -> R` | ✓ | combine results with `Monoid<R>` (`OpDecidable` needs a `Class<R>`) |
| `Const<C, A>` | a value of `C` | ✓ | – |

`contramap` is a member; `divide`, `choose`, `conquer` and `lose` are only on the instance interfaces
(`PredicateDecidable`, `EquivalenceDecidable`, `ComparisonDecidable`, `OpDecidable<R>`).

## Contramap

```kotlin
import com.github.kotlinz.kotlinz.data.comparison.Comparison
import com.github.kotlinz.kotlinz.data.equivalence.Equivalence
import com.github.kotlinz.kotlinz.data.predicate.Predicate

data class User(val name: String, val age: Int)

val isAdult = Predicate { age: Int -> age >= 18 } contramap { u: User -> u.age }
val byName = Comparison.natural<String>() contramap { u: User -> u.name }
val sameNameIgnoringCase = Equivalence.default<String>() contramap { u: User -> u.name.lowercase() }

check(isAdult.test(User("ann", 20)))
check(!isAdult.test(User("bob", 12)))
check(listOf(User("bob", 1), User("ann", 2)).sortedWith(byName.toComparator()).map { it.name } == listOf("ann", "bob"))
check(sameNameIgnoringCase.equiv(User("Ann", 1), User("ANN", 2)))
```

## Divide and choose

```kotlin
import com.github.kotlinz.kotlinz.data.comparison.Comparison
import com.github.kotlinz.kotlinz.data.comparison.ComparisonDecidable
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.predicate.Predicate
import com.github.kotlinz.kotlinz.data.predicate.PredicateDecidable

data class User(val name: String, val age: Int)

val comparisons = object : ComparisonDecidable {}
val byAgeThenName = Comparison.narrow(comparisons.divide(
  { u: User -> Pair(u.age, u.name) }, Comparison.natural<Int>(), Comparison.natural<String>()
))
val users = listOf(User("bob", 30), User("ann", 30), User("cid", 20))
check(users.sortedWith(byAgeThenName.toComparator()).map { it.name } == listOf("cid", "ann", "bob"))

val predicates = object : PredicateDecidable {}
val validInput = Predicate.narrow(predicates.choose(
  { s: String -> s.toIntOrNull()?.let { Either.Right<String, Int>(it) } ?: Either.Left(s) },
  Predicate { s: String -> s.isNotBlank() },
  Predicate { n: Int -> n > 0 }
))
check(validInput.test("42"))
check(!validInput.test("-1"))
check(validInput.test("text"))
check(Predicate.narrow(predicates.conquer<Int>()).test(0))
```

`Op` needs a monoid to combine the two results of `divide`:

```kotlin
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.op.Op
import com.github.kotlinz.kotlinz.data.op.OpDecidable

val sizes = object : OpDecidable<SumMonoid> { override val type = SumMonoid::class.java }
val totalSize = Op.narrow(sizes.divide(
  { p: Pair<String, List<Int>> -> p }, Op { s: String -> SumMonoid(s.length) }, Op { l: List<Int> -> SumMonoid(l.size) }
))
check(totalSize.run(Pair("abc", listOf(1, 2))) == SumMonoid(5))
```

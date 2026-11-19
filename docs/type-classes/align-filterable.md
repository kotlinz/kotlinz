# Align, Zip, Filterable and Witherable

Two small families of functor extensions for container-like types: combining two structures by shape
(`Semialign`, `Align`, `Zip`) and dropping elements (`Filterable`, `Witherable`).

## Semialign, Align and Zip

<!-- nocheck -->
```kotlin
interface Semialign<T>: Functor<T> {
  fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<T, A>, b: K1<T, B>): K1<T, C>
  fun <A, B> align(a: K1<T, A>, b: K1<T, B>): K1<T, Ior<A, B>>
}
interface Align<T>: Semialign<T> { fun <A> nil(): K1<T, A> }
interface Zip<T>: Semialign<T> {
  fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<T, A>, b: K1<T, B>): K1<T, C>
  fun <A, B> zip(a: K1<T, A>, b: K1<T, B>): K1<T, Pair<A, B>>
}
```

`align` is a "union" of shapes: positions present on both sides become `Ior.Both`, positions present on only one
side become `Ior.Left` / `Ior.Right`, so nothing is lost. `zip` is the "intersection": only positions present on
both sides survive. `Align.nil` is the empty structure (the unit of `align`).

| Data type | `Semialign` | `Align` (`nil`) | `Zip` |
|---|---|---|---|
| `Maybe`, `ListK`, `LazyList`, `Chain` | ✓ | ✓ | ✓ |
| `NonEmptyList`, `Tree` | ✓ | – (never empty) | ✓ |
| `NonEmptyMap<K, _>` | ✓ (union of keys) | – | – |

`align`, `alignWith`, `zip` and `zipWith` are members; `nil()` is on the companions of the `Align` types.

```kotlin
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf

check((listKOf(1, 2, 3) align listKOf("a")) == listKOf(Ior.both(1, "a"), Ior.left(2), Ior.left(3)))
check((listKOf(1, 2, 3) zip listKOf("a")) == listKOf(Pair(1, "a")))

val padded = listKOf(1, 2, 3).alignWith(listKOf(10)) { ior ->
  when (ior) {
    is Ior.Both -> ior.left + ior.right
    is Ior.Left -> ior.value
    is Ior.Right -> ior.value
  }
}
check(padded == listKOf(11, 2, 3))

check((Maybe.Just(1) align Maybe.None<String>()) == Maybe.Just(Ior.left<Int, String>(1)))
check((Maybe.Just(1) zip Maybe.None<String>()) == Maybe.None<Pair<Int, String>>())
check(ListK.nil<Int>() == listKOf<Int>())
check(nonEmptyListOf(1, 2).zipWith(nonEmptyListOf(10, 20, 30)) { a, b -> a + b } == nonEmptyListOf(11, 22))
```

`Zip` is related to [`MonadZip`](functor-applicative-monad.md#monadzip) (`mzip`), but does not require a monad, so it
also covers types such as `NonEmptyList` and `Tree`.

## Filterable and Witherable

<!-- nocheck -->
```kotlin
interface Filterable<T>: Functor<T> {
  fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<T, A>): K1<T, B>
  fun <A> filter(p: (A) -> Boolean, v: K1<T, A>): K1<T, A>
  fun <A, B: Any> collect(f: (A) -> B?, v: K1<T, A>): K1<T, B>   // nullable-returning variant of mapFilter
  fun <A> catMaybes(v: K1<T, Maybe<A>>): K1<T, A>
}
interface Witherable<T>: Traversable<T>, Filterable<T> {
  fun <G, A, B> wither(applicative: Applicative<G>, f: (A) -> K1<G, Maybe<B>>, v: K1<T, A>): K1<G, K1<T, B>>
  fun <G, A> filterA(applicative: Applicative<G>, p: (A) -> K1<G, Boolean>, v: K1<T, A>): K1<G, K1<T, A>>
}
```

`Filterable` generalizes `filter` / `mapNotNull` to any functor. `Witherable` is the effectful version: it filters
while traversing with an `Applicative`, so the decision to keep an element can itself be an effect.

Instances: `Maybe`, `ListK`, `LazyList`, `Chain` (members `mapFilter`, `filter`, `collect`, `wither`, `filterA`
and companion `catMaybes`), and `Either<L, _>` when `L` is a `Monoid`: a dropped `Right` becomes `Left(mzero)`, so the
`Either` helpers are extension functions that take the monoid class.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.filter
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

check((listKOf("1", "x", "3") mapFilter ::parse) == listKOf(1, 3))
check((listKOf("1", "x", "3") collect { it.toIntOrNull() }) == listKOf(1, 3))
check((listKOf(1, 2, 3, 4) filter { it % 2 == 0 }) == listKOf(2, 4))
check(ListK.catMaybes(listKOf(Maybe.Just(1), Maybe.None(), Maybe.Just(3))) == listKOf(1, 3))
check((Maybe.Just(3) filter { it > 5 }) == Maybe.None<Int>())

val maybe = object : MaybeApplicative {}
val keepSmall = { n: Int -> if (n < 0) Maybe.None() else Maybe.Just(n < 10) }
check(Maybe.narrow(listKOf(1, 20, 3).filterA(maybe, keepSmall)) == Maybe.Just(listKOf(1, 3)))
check(Maybe.narrow(listKOf(1, -1, 3).filterA(maybe, keepSmall)) == Maybe.None<ListK<Int>>())

val right: Either<StringMonoid, Int> = Either.Right(1)
check(right.filter(StringMonoid::class.java) { it > 5 } == Either.Left<StringMonoid, Int>(StringMonoid("")))
```

`filterA` with `Maybe` fails as a whole when any predicate fails, just like `traverse`. With `ListK` as the
applicative it enumerates every subset that the predicate allows.

# Foldable and Traversable

This page covers `Foldable` / `Traversable`, their non-empty refinements `Foldable1` / `Traversable1`, and the
two-parameter versions `Bifoldable` / `Bitraversable`.

## Foldable

<!-- nocheck -->
```kotlin
interface Foldable<T> {
  fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<T, A>): B
  fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<T, A>): B
  fun <A, M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M, v: K1<T, A>): M
  fun <A> toList(v: K1<T, A>): List<A>
  fun <A> length(v: K1<T, A>): Int
  fun <A> isEmpty(v: K1<T, A>): Boolean
}
```

Instances: `Identity`, `Maybe`, `Either<L, _>` (folds the `Right`), `Try` (folds the `Success`), `ListK`,
`NonEmptyList`, `Tuple2<S, _>`, the [data structures](../data-structures.md) `LazyList`, `Chain`, `Tree`,
`NonEmptySet`, `NonEmptyMap<K, _>` and `ListZipper`, and the combinators `Compose`, `Product`, `Coproduct`.

`foldl`, `foldr` and `foldMap` are members on these data types. `toList`, `length` and `isEmpty` are only on the
instance interfaces.

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toSumMonoid

val numbers = listKOf(1, 2, 3, 4)

check(numbers.foldl(0) { acc, x -> acc + x } == 10)
check(numbers.foldr("") { x, acc -> "$x$acc" } == "1234")
check(numbers.foldMap(SumMonoid::class.java) { it.toSumMonoid() } == SumMonoid(10))
check(numbers.foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } == StringMonoid("1234"))
check(Maybe.None<Int>().foldl(42) { acc, x -> acc + x } == 42)

val foldable = object : ListKFoldable {}
check(foldable.length(numbers) == 4)
check(foldable.toList(numbers) == listOf(1, 2, 3, 4))
check(foldable.isEmpty(listKOf<Int>()))
```

`foldMap` takes a `Class<M>` (see [Higher-kinded types](../hkt.md#passing-monoids-and-semigroups-classm)); the
available monoids are listed in [Monoids](../monoids.md).

Note that the default `toList` builds the list with `foldr` and list concatenation, so it is quadratic. For
`ListK`, read `.value`; for `NonEmptyList`, call the member `toList()`.

## Traversable

<!-- nocheck -->
```kotlin
interface Traversable<T>: Functor<T>, Foldable<T> {
  fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<T, A>): K1<G, K1<T, B>>
  fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>>
}
```

`traverse` maps every element to an effect and combines the effects with the given `Applicative`, turning
"a list of results" inside out into "a result of a list". Instances: `Identity`, `Maybe`, `Either<L, _>`, `Try`,
`ListK`, `NonEmptyList`, `Tuple2<S, _>`, `LazyList`, `Chain`, `Tree`, `NonEmptyMap<K, _>`, `ListZipper`. `traverse` is
a member; `sequence` is on the companions. Types that are never empty are also
[`Traversable1`](#foldable1-and-traversable1), and `Maybe`, `ListK`, `LazyList`, `Chain` and `Either` are also
[`Witherable`](align-filterable.md#filterable-and-witherable) (traverse and filter in one pass).

The applicative decides the semantics: `Maybe` / `Either` short-circuit on the first failure, `Validated` collects
every error, `ListK` produces every combination.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.EitherMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()

val maybe = object : MaybeApplicative {}
check(Maybe.narrow(listKOf("1", "2", "3").traverse(maybe, ::parse)) == Maybe.Just(listKOf(1, 2, 3)))
check(Maybe.narrow(listKOf("1", "x", "3").traverse(maybe, ::parse)) == Maybe.None<ListK<Int>>())

val either = object : EitherMonad<String> {}
val firstError = listKOf("1", "x", "y").traverse(either) { s ->
  s.toIntOrNull()?.let { Either.Right<String, Int>(it) } ?: Either.Left("bad: $s")
}
check(Either.narrow(firstError) == Either.Left<String, ListK<Int>>("bad: x"))

check(ListK.sequence(maybe, listKOf(Maybe.Just(1), Maybe.Just(2))) == Maybe.Just(listKOf(1, 2)))
check(ListK.sequence(object : ListKApplicative {}, listKOf(listKOf(1, 2), listKOf(3))) == listKOf(listKOf(1, 3), listKOf(2, 3)))
```

### Accumulating errors with `Validated`

Traversing with `ValidatedApplicative` reports every failure instead of stopping at the first one. It needs a
`Semigroup` class for the error type:

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.listk.toListK
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.validated.Validated
import com.github.kotlinz.kotlinz.data.validated.ValidatedApplicative

val validated = object : ValidatedApplicative<ListMonoid> { override val type = ListMonoid::class.java }

fun parseAll(input: List<String>) = Validated.narrow(input.toListK().traverse(validated) { s ->
  s.toIntOrNull()?.let { Validated.valid<ListMonoid, Int>(it) } ?: Validated.invalid(ListMonoid(listOf("bad: $s")))
})

check(parseAll(listOf("1", "2")) == Validated.valid<ListMonoid, ListK<Int>>(listKOf(1, 2)))
check(parseAll(listOf("1", "x", "y")) == Validated.invalid<ListMonoid, ListK<Int>>(ListMonoid(listOf("bad: x", "bad: y"))))
```

### Type inference caveat

`traverse` returns `K1<G, K1<T, B>>`; wrap the result with the outer type's `narrow` (`Maybe.narrow`,
`Either.narrow`, ...) to get a concrete value. The inner value is still typed `K1<T, B>`, but it *is* a `ListK<B>`
(or whatever `T` is), so equality checks against concrete values work as shown above.

## Foldable1 and Traversable1

<!-- nocheck -->
```kotlin
interface Foldable1<T>: Foldable<T> {
  fun <A> reduceLeft(f: (A, A) -> A, v: K1<T, A>): A
  fun <A> reduceRight(f: (A, A) -> A, v: K1<T, A>): A
  fun <A, S: Semigroup<*, S>> reduceMap(type: Class<S>, f: (A) -> S, v: K1<T, A>): S
  fun <A> head(v: K1<T, A>): A
  fun <A> last(v: K1<T, A>): A
}
interface Traversable1<T>: Traversable<T>, Foldable1<T> {
  fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<T, A>): K1<G, K1<T, B>>
  fun <G, A> sequence1(apply: Apply<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>>
}
```

A structure that always holds at least one element can be folded without an initial value (into a `Semigroup`
instead of a `Monoid`) and traversed with an [`Apply`](functor-applicative-monad.md#apply-and-flatmap), an
applicative without `pure`. Instances: `Identity`, `NonEmptyList`, `Tuple2<S, _>`, `Tree`, `NonEmptyMap<K, _>`,
`ListZipper` (both), and `NonEmptySet` (`Foldable1` only). `NonEmptyList` and `Tree` expose `reduceLeft`,
`reduceMap` and `traverse1` as members.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListTraversable1
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2Functor
import com.github.kotlinz.kotlinz.type.applicative.Apply

check(nonEmptyListOf(1, 2, 3).reduceLeft { a, b -> a - b } == -4)
check(nonEmptyListOf(1, 2).reduceMap(StringMonoid::class.java) { StringMonoid("$it") } == StringMonoid("12"))

val instance = object : NonEmptyListTraversable1 {}
check(instance.reduceRight({ a, b -> a - b }, nonEmptyListOf(1, 2, 3)) == 2)
check(instance.last(nonEmptyListOf(1, 2, 3)) == 3)

// A log over String concatenation: an Apply with no `pure`, so it cannot traverse a possibly empty list.
object Log : Apply<K1<Tuple2.T, String>>, Tuple2Functor<String> {
  override fun <A, B> ap(f: K1<K1<Tuple2.T, String>, (A) -> B>, v: K1<K1<Tuple2.T, String>, A>): Tuple2<String, B> {
    val tf = Tuple2.narrow(f)
    val tv = Tuple2.narrow(v)
    return Tuple2(tf.first + tv.first, tf.second(tv.second))
  }
}

val logged = nonEmptyListOf(1, 2, 3).traverse1(Log) { n -> Tuple2("[$n]", n * 2) }
check(logged == Tuple2("[1][2][3]", nonEmptyListOf(2, 4, 6)))
```

## Bifoldable and Bitraversable

<!-- nocheck -->
```kotlin
interface Bifoldable<T> {
  fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<T, A, B>): C
  fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<T, A, B>): C
  fun <A, B, M: Monoid<*, M>> bifoldMap(type: Class<M>, f: (A) -> M, g: (B) -> M, v: K2<T, A, B>): M
}
interface Bitraversable<T>: Bifunctor<T>, Bifoldable<T> {
  fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<T, A, B>): K1<G, K2<T, C, D>>
  fun <G, A, B> bisequence(applicative: Applicative<G>, v: K2<T, K1<G, A>, K1<G, B>>): K1<G, K2<T, A, B>>
}
```

The two-parameter versions fold or traverse *both* sides of a [`Bifunctor`](bifunctor-profunctor-arrow.md#bifunctor).
Instances: `Either`, `Ior`, `Tuple2`, `Validated`. `bifoldLeft`, `bifoldRight`, `bifoldMap` and `bitraverse` are
members; `bisequence` is on the companions. `Ior.Both` and `Tuple2` visit the left value before the right one.

```kotlin
import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.tuple2.Tuple2

check(Ior.both("a", 1).bifoldLeft("", { acc, a -> acc + a }, { acc, b -> "$acc$b" }) == "a1")
check(Either.Right<String, Int>(2).bifoldRight(0, { s, acc -> acc + s.length }, { n, acc -> acc + n }) == 2)
check(Tuple2("x", 2).bifoldMap(StringMonoid::class.java, { StringMonoid(it) }, { StringMonoid("$it") }) == StringMonoid("x2"))

fun parse(s: String): Maybe<Int> = s.toIntOrNull()?.let { Maybe.Just(it) } ?: Maybe.None()
val maybe = object : MaybeApplicative {}
check(Maybe.narrow(Tuple2("1", "2").bitraverse(maybe, ::parse, ::parse)) == Maybe.Just(Tuple2(1, 2)))
check(Maybe.narrow(Either.Left<String, String>("x").bitraverse(maybe, ::parse, ::parse)) == Maybe.None<Either<Int, Int>>())
val nested = Either.Right<K1<Maybe.T, Int>, K1<Maybe.T, Int>>(Maybe.Just(1))
check(Either.bisequence(maybe, nested) == Maybe.Just(Either.Right<Int, Int>(1)))
```

# Foldable and Traversable

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
`NonEmptyList`, and the combinators `Compose`, `Product`, `Coproduct`.

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
`ListK`, `NonEmptyList`. `traverse` is a member; `sequence` is on the companions.

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

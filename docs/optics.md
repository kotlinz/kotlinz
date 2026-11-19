# Optics

Optics are first-class, composable getters and setters for immutable data. They live in
`com.github.kotlinz.kotlinz.optics` and are **profunctor-encoded**: every optic is a function that lifts a
profunctor value `P<A, B>` (acting on the focus) to `P<S, T>` (acting on the whole structure), so composing two
optics is plain function composition. You never need to touch profunctors directly: every optic comes with
`get` / `set` / `modify`-style helpers.

| Optic | Focus | Read | Write | Built from |
|---|---|---|---|---|
| [`Iso<S, T, A, B>`](#iso) | exactly one, reversible | `get` | `reverseGet`, `modify` | `Iso(get, reverseGet)` |
| [`Lens<S, T, A, B>`](#lens) | exactly one | `get` | `set`, `modify` | `Lens(get, set)` |
| [`Prism<S, T, A, B>`](#prism) | one case of a sum type | `getMaybe` | `reverseGet`, `modify` | `Prism(getMaybe, reverseGet)`, `Prism.matching` |
| [`Optional<S, T, A, B>`](#optional) | zero or one | `getMaybe` | `set`, `modify` | `Optional(getMaybe, set)`, `Optional.matching` |
| [`Traversal<S, T, A, B>`](#traversal) | zero or more | `getAll` | `set`, `modify`, `traverseOf` | `Traversal(wander)`, `Traversal.fromTraversable` |
| [`Getter<S, A>`](#getter-and-fold) | exactly one, read-only | `view` | | `Getter(get)` |
| [`Fold<S, A>`](#getter-and-fold) | zero or more, read-only | `toListOf`, `preview`, `foldMapOf` | | `Fold(toList)`, `Fold.fromFoldable` |
| [`Setter<S, T, A, B>`](#setter) | zero or more, write-only | | `over`, `set` | `Setter(over)`, `Setter.fromFunctor` |

The four type parameters make optics *polymorphic*: replacing the `A` inside an `S` with a `B` may change the
structure's type to `T`. For the common simple case `S == T` and `A == B`, the factories `Lens(get, set)`,
`Prism(getMaybe, reverseGet)` and `Optional(getMaybe, set)` infer `Lens<S, S, A, A>` and so on.

## The hierarchy

<!-- nocheck -->
```text
            Iso
          /     \
       Lens     Prism
       /  \      /
  Getter   Optional
     |        |
     |    Traversal
     |     /     \
     Fold        Setter
```

An arrow means "is a": every `Iso` is a `Lens` and a `Prism`, every `Lens` is an `Optional` and a `Getter`, and
so on. Composition is a single `andThen` operator, overloaded once per optic kind. When you compose two different
kinds, Kotlin picks the overload of their most specific common supertype, so `lens andThen prism` is an
`Optional`, `lens andThen Traversal.list()` is a `Traversal`, and `prism andThen getter` is a `Fold`. All helpers are
extension functions, so the most specific helper also wins (`iso.get(s)` uses `Iso.get`, not `Lens.get`).

`outer andThen inner` focuses through `outer` first, like Monocle. There is deliberately no `compose`, because
`Function1.compose` reads in the opposite direction.

## Iso

An isomorphism: two mutually inverse functions. Laws: `reverseGet(get(s)) == s` and `get(reverseGet(b)) == b`.

```kotlin
import com.github.kotlinz.kotlinz.optics.Iso
import com.github.kotlinz.kotlinz.optics.get
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.reverse
import com.github.kotlinz.kotlinz.optics.reverseGet

val chars = Iso({ s: String -> s.toList() }, { cs: List<Char> -> cs.joinToString("") })

check(chars.get("ab") == listOf('a', 'b'))
check(chars.reverseGet(listOf('x', 'y')) == "xy")
check(chars.modify { it.reversed() }("abc") == "cba")
check(chars.reverse().get(listOf('o', 'k')) == "ok")
```

`Iso.id()` is the identity isomorphism.

## Lens

A focus on exactly one part of a product, typically a field of a data class. `set` and `modify` are curried and
return `(S) -> T`. Laws: get-set, set-get and set-set.

```kotlin
import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.andThen
import com.github.kotlinz.kotlinz.optics.first
import com.github.kotlinz.kotlinz.optics.get
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.set

data class Address(val city: String, val zip: String)
data class Person(val name: String, val address: Address)

val address = Lens({ p: Person -> p.address }, { p: Person, a: Address -> p.copy(address = a) })
val city = Lens({ a: Address -> a.city }, { a: Address, c: String -> a.copy(city = c) })
val personCity = address andThen city

val ann = Person("Ann", Address("Tokyo", "100-0001"))

check(personCity.get(ann) == "Tokyo")
check(personCity.set("Kyoto")(ann) == Person("Ann", Address("Kyoto", "100-0001")))
check(personCity.modify { it.uppercase() }(ann).address.city == "TOKYO")

// Lens.first() / Lens.second() focus on a component of a Pair, and may change its type.
check(Lens.first<Int, String, Boolean>().set("one")(1 to true) == ("one" to true))
```

## Prism

A focus on one case of a sum type: `getMaybe` succeeds only for that case, and `reverseGet` builds the whole
value from the focus. `modify` leaves non-matching values untouched.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.optics.Prism
import com.github.kotlinz.kotlinz.optics.getMaybe
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.reverseGet

sealed class Shape {
  data class Circle(val radius: Int) : Shape()
  data class Square(val side: Int) : Shape()
}

val circle: Prism<Shape, Shape, Int, Int> = Prism({ s: Shape -> if (s is Shape.Circle) Maybe.Just(s.radius) else Maybe.None() }, { r: Int -> Shape.Circle(r) })

check(circle.getMaybe(Shape.Circle(2)) == Maybe.Just(2))
check(circle.getMaybe(Shape.Square(3)) == Maybe.None<Int>())
check(circle.reverseGet(5) == Shape.Circle(5))
check(circle.modify { it * 10 }(Shape.Circle(1)) == Shape.Circle(10))
check(circle.modify { it * 10 }(Shape.Square(1)) == Shape.Square(1))
```

For a polymorphic prism use `Prism.matching(match, reverseGet)`, where `match: (S) -> Either<T, A>` returns either
the focus or the already rebuilt `T`. (It has a distinct name because Kotlin cannot choose between overloads that
differ only in a lambda's return type.)

Ready-made prisms live on the companion: `Prism.just()` and `Prism.none()` for `Maybe`, `Prism.left()` and
`Prism.right()` for `Either`.

```kotlin
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.optics.Prism
import com.github.kotlinz.kotlinz.optics.getMaybe
import com.github.kotlinz.kotlinz.optics.just
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.right

val rightValue = Prism.right<Int, Int, String>()

check(rightValue.getMaybe(Either.Right<String, Int>(1)) == Maybe.Just(1))
check(rightValue.modify { it + 1 }(Either.Left<String, Int>("e")) == Either.Left<String, Int>("e"))
check(Prism.just<Int, String>().modify { "#$it" }(Maybe.Just(7)) == Maybe.Just("#7"))
```

## Optional

A focus on *at most one* value, also called an affine traversal. It is what you get when you compose a `Lens` with
a `Prism`, and you can also build one directly with `Optional(getMaybe, set)`. The setter only runs when the
focus exists.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.Optional
import com.github.kotlinz.kotlinz.optics.Prism
import com.github.kotlinz.kotlinz.optics.andThen
import com.github.kotlinz.kotlinz.optics.getMaybe
import com.github.kotlinz.kotlinz.optics.just
import com.github.kotlinz.kotlinz.optics.set

data class Account(val id: Int, val email: Maybe<String>)

val email = Lens({ a: Account -> a.email }, { a: Account, e: Maybe<String> -> a.copy(email = e) })
val emailAddress: Optional<Account, Account, String, String> = email andThen Prism.just<String, String>()

check(emailAddress.getMaybe(Account(1, Maybe.Just("a@example.com"))) == Maybe.Just("a@example.com"))
check(emailAddress.getMaybe(Account(2, Maybe.None())) == Maybe.None<String>())
check(emailAddress.set("b@example.com")(Account(1, Maybe.Just("a@example.com"))) == Account(1, Maybe.Just("b@example.com")))
check(emailAddress.set("b@example.com")(Account(2, Maybe.None())) == Account(2, Maybe.None()))

val head = Optional({ xs: List<Int> -> xs.firstOrNull()?.let { Maybe.Just(it) } ?: Maybe.None() },
  { xs: List<Int>, x: Int -> listOf(x) + xs.drop(1) })
check(head.set(9)(listOf(1, 2)) == listOf(9, 2))
check(head.set(9)(listOf()) == listOf<Int>())
```

## Traversal

A focus on zero or more values, visited in order. Besides `getAll`, `set` and `modify`, a traversal can run an
effect on every focus with `traverseOf(applicative, f, s)`, which collects the effects of any `Applicative`
(for example, validating every element and failing on the first `None`).

Ready-made traversals: `Traversal.list()`, `Traversal.listK()`, `Traversal.nonEmptyList()`, `Traversal.maybe()`
and, for any `Traversable` instance, `Traversal.fromTraversable(traversable)`. Custom traversals are built from a
van Laarhoven `Wander` with `Traversal(wander)`.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.Traversal
import com.github.kotlinz.kotlinz.optics.andThen
import com.github.kotlinz.kotlinz.optics.getAll
import com.github.kotlinz.kotlinz.optics.list
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.traverseOf

data class Item(val name: String, val price: Int)
data class Order(val id: Int, val items: List<Item>)

val items = Lens({ o: Order -> o.items }, { o: Order, xs: List<Item> -> o.copy(items = xs) })
val price = Lens({ i: Item -> i.price }, { i: Item, p: Int -> i.copy(price = p) })
val prices = items andThen Traversal.list<Item, Item>() andThen price

val order = Order(1, listOf(Item("tea", 300), Item("cake", 450)))

check(prices.getAll(order) == listOf(300, 450))
check(prices.modify { it * 2 }(order) == Order(1, listOf(Item("tea", 600), Item("cake", 900))))

val each = Traversal.list<Int, Int>()
val positive = { n: Int -> if (n > 0) Maybe.Just(n) else Maybe.None() }
check(Maybe.narrow(each.traverseOf(object : MaybeMonad {}, positive, listOf(1, 2))) == Maybe.Just(listOf(1, 2)))
check(Maybe.narrow(each.traverseOf(object : MaybeMonad {}, positive, listOf(1, -2))) == Maybe.None<List<Int>>())
```

## Getter and Fold

`Getter<S, A>` and `Fold<S, A>` are read-only: they only have the `S` and `A` parameters. A `Getter` is just a
function `S -> A` as an optic (`view`); a `Fold` reads zero or more values (`toListOf`, `preview` for the first
one, and `foldMapOf` to combine them with a `MonoidOf`). Every `Lens` is a `Getter`, and every `Traversal` and
`Getter` is a `Fold`, so they compose with the writable optics.

```kotlin
import com.github.kotlinz.kotlinz.data.listk.ListKFoldable
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.optics.Fold
import com.github.kotlinz.kotlinz.optics.Getter
import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.Prism
import com.github.kotlinz.kotlinz.optics.Traversal
import com.github.kotlinz.kotlinz.optics.andThen
import com.github.kotlinz.kotlinz.optics.first
import com.github.kotlinz.kotlinz.optics.foldMapOf
import com.github.kotlinz.kotlinz.optics.fromFoldable
import com.github.kotlinz.kotlinz.optics.just
import com.github.kotlinz.kotlinz.optics.list
import com.github.kotlinz.kotlinz.optics.preview
import com.github.kotlinz.kotlinz.optics.toListOf
import com.github.kotlinz.kotlinz.optics.view

val length = Getter { s: String -> s.length }
val digits = Fold { s: String -> s.filter(Char::isDigit).toList() }
val sum = MonoidOf(0) { a: Int, b: Int -> a + b }

check(length.view("abc") == 3)
check((Lens.first<String, String, Int>() andThen length).view("ab" to 1) == 2)
check(digits.toListOf("a1b2") == listOf('1', '2'))
check(digits.preview("abc") == Maybe.None<Char>())
check(digits.foldMapOf(sum, { it.digitToInt() }, "a1b2") == 3)
check((Traversal.list<String, String>() andThen digits).toListOf(listOf("a1", "2b")) == listOf('1', '2'))
check((Prism.just<String, String>() andThen length).preview(Maybe.Just("abc")) == Maybe.Just(3))
check(Fold.fromFoldable<_, Int>(object : ListKFoldable {}).toListOf(listKOf(1, 2)) == listOf(1, 2))
```

`preview` keeps the first `Just` it sees, using an internal first-`Just` monoid.

The optics `Fold` is unrelated to the composable left fold `com.github.kotlinz.kotlinz.data.fold.Fold` described in
[Automata and folds](automata.md#fold); import the one you need explicitly.

## Setter

A write-only focus on zero or more values, the dual of `Fold`. `over(f)` maps every focus and `set(b)` replaces it.
`Setter.fromFunctor(functor)` turns any `Functor` into a setter. Every `Traversal` (hence every `Optional`,
`Lens`, `Prism` and `Iso`) is a `Setter`.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.Setter
import com.github.kotlinz.kotlinz.optics.andThen
import com.github.kotlinz.kotlinz.optics.fromFunctor
import com.github.kotlinz.kotlinz.optics.over
import com.github.kotlinz.kotlinz.optics.second
import com.github.kotlinz.kotlinz.optics.set

val values = Setter { f: (Int) -> String -> { m: Map<String, Int> -> m.mapValues { f(it.value) } } }

check(values.over { "#$it" }(mapOf("a" to 1)) == mapOf("a" to "#1"))
check(values.set("x")(mapOf("a" to 1, "b" to 2)) == mapOf("a" to "x", "b" to "x"))

val inMaybe = Setter.fromFunctor<Maybe.T, Int, Int>(object : MaybeFunctor {})
check(Maybe.narrow(inMaybe.over { it + 1 }(Maybe.Just(1))) == Maybe.Just(2))

val secondValues = Lens.second<Map<String, Int>, Map<String, Int>, String>() andThen
  Setter { f: (Int) -> Int -> { m: Map<String, Int> -> m.mapValues { f(it.value) } } }
check(secondValues.set(7)("x" to mapOf("a" to 1)) == ("x" to mapOf("a" to 7)))
```

## How the profunctor encoding works

Each optic kind is an interface with one generic method constrained by a profunctor class from
[Bifunctor, Profunctor and Arrow](type-classes/bifunctor-profunctor-arrow.md). Kotlin has no rank-2 types, so
"for every profunctor `P`" is expressed as a generic method:

<!-- nocheck -->
```kotlin
interface Iso<S, T, A, B> : Lens<S, T, A, B>, Prism<S, T, A, B> {
  fun <P, F> runProfunctor(profunctor: Profunctor<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>
}
interface Lens<S, T, A, B> : Optional<S, T, A, B>, Getter<S, A> {
  fun <P, F> runStrong(strong: Strong<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>
}
interface Prism<S, T, A, B> : Optional<S, T, A, B> {
  fun <P, F> runChoice(choice: Choice<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>
}
interface Optional<S, T, A, B> : Traversal<S, T, A, B> {
  fun <P, F, Q> runOptional(profunctor: Q, pab: K3<P, F, A, B>): K3<P, F, S, T> where Q : Strong<P, F>, Q : Choice<P, F>
}
interface Traversal<S, T, A, B> : Fold<S, A>, Setter<S, T, A, B> {
  fun <P, F> runTraversing(traversing: Traversing<P, F>, pab: K3<P, F, A, B>): K3<P, F, S, T>
}
```

The helpers simply pick a concrete profunctor:

| Helper | Profunctor |
|---|---|
| `get`, `view`, `getAll`, `toListOf`, `foldMapOf` | `Forget<R, A, B>` (with a monoid when there can be several foci) |
| `getMaybe`, `preview` | `Forget` with the first-`Just` monoid |
| `reverseGet` | `Tagged<A, B>` |
| `set`, `modify`, `over` | `Function1<A, B>` |
| `traverseOf` | `Star<G, A, B>` for an `Applicative<G>` |

A `Setter` is encoded directly against `Function1` (its only useful strict instance) instead of a separate
`Mapping` class, and `Getter` / `Fold` are encoded against `Forget` alone.

## Caveats

- Type arguments of the ready-made optics (`Lens.first<A, B, C>()`, `Prism.just<A, B>()`, `Traversal.list<A, B>()`)
  usually have to be spelled out, because Kotlin cannot infer them from a later `andThen`.
- `Prism` has no `set` of its own; `prism.set(b)` resolves to `Optional.set` and only replaces a matching case.
  (A dedicated `Prism.set` would make `Iso.set` ambiguous, since an `Iso` is both a `Lens` and a `Prism`.)
- Annotate the optic type when a constructor lambda returns a subtype: in `Prism({ s: Shape -> ... }, { r: Int ->
  Shape.Circle(r) })`, Kotlin would otherwise infer `S = Shape.Circle` from the second lambda.
- Optics are not generated from data classes; write lenses by hand with `copy`.

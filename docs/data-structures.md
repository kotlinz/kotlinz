# Data structures

Persistent collections beyond `ListK` and `NonEmptyList` (see [Data types](data-types.md#collections-and-products)).
Each lives in its own package under `com.github.kotlinz.kotlinz.data` and follows the usual conventions: a `T`
tag, `narrow`, one `<Type><TypeClass>` interface per instance, and the common operations as already-narrowed
members.

| Type | Package | What it is | Instances |
|---|---|---|---|
| [`LazyList<A>`](#lazylist) | `data.lazylist` | lazy, memoized, possibly infinite list | `Monad`, `Alternative`, `Traversable`, `Align`, `Zip`, `Witherable` |
| [`Chain<A>`](#chain) | `data.chain` | catenable sequence with O(1) `+`, `prepend`, `append` | `Monad`, `Traversable`, `Align`, `Zip`, `Witherable`, `Monoid` |
| [`Tree<A>`](#tree) | `data.tree` | rose tree (a root and a list of subtrees) | `Monad`, `Traversable1`, `Comonad`, `Zip` |
| [`NonEmptySet<A>`](#nonemptyset) | `data.nonemptyset` | insertion-ordered set with at least one element | `Foldable1` |
| [`NonEmptyMap<K, V>`](#nonemptymap) | `data.nonemptymap` | insertion-ordered map with at least one entry | `Functor`, `Traversable1`, `Semialign` (over the values) |
| [`ListZipper<A>`](#listzipper) | `data.zipper` | non-empty list with a cursor | `Traversable1`, `Comonad` |

`Foldable1` / `Traversable1` (folds and traversals that need no empty value, so only an `Apply` or a `Semigroup`)
are described in [Foldable and Traversable](type-classes/foldable-traversable.md); `Semialign` / `Align` / `Zip`
and `Filterable` / `Witherable` in [Align and Filterable](type-classes/align-filterable.md). The full matrix is in
[Type classes](type-classes/README.md#coverage).

## LazyList

A lazy list whose cells are computed on demand and memoized, so it can be infinite. `fmap`, `bind`, `filter`,
`take`, `zip` and friends are lazy; operations that must see every element (`foldl`, `traverse`, `toList`,
`equals`, `hashCode`) only terminate on finite lists. `toString` never forces evaluation: unevaluated cells print
as `?`.

Builders: `LazyList.empty()`, `LazyList.cons(head) { tail }`, `LazyList.defer { ... }`, `lazyListOf(...)`,
`List.toLazyList()`, `LazyList.iterate(seed, f)`, `LazyList.repeat(value)` and `LazyList.unfold(seed, f)`.

```kotlin
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.lazylist.LazyList
import com.github.kotlinz.kotlinz.data.lazylist.lazyListOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe

val naturals = LazyList.iterate(0) { it + 1 }

check((naturals fmap { it * it }).take(4).toList() == listOf(0, 1, 4, 9))
check(naturals.filter { it % 3 == 0 }.drop(1).head() == Maybe.Just(3))
check(naturals.takeWhile { it < 3 }.toList() == listOf(0, 1, 2))

fun fibs(a: Long, b: Long): LazyList<Long> = LazyList.cons(a) { fibs(b, a + b) }
check(fibs(0, 1).take(8).toList() == listOf(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L))

val countdown = LazyList.unfold(3) { n -> if (n == 0) Maybe.None() else Maybe.Just(Pair(n, n - 1)) }
check(countdown.toList() == listOf(3, 2, 1))

// Zip stops at the shorter list (an infinite one is fine), align keeps both ends.
check((naturals zip lazyListOf("a", "b")).toList() == listOf(0 to "a", 1 to "b"))
check((lazyListOf(1, 2) align lazyListOf("a")).toList() == listOf(Ior.Both(1, "a"), Ior.Left<Int, String>(2)))

val evaluated = naturals.take(3)
evaluated.head()
check(evaluated.toString() == "LazyList(0, ?)")
check(LazyList.fx { lazyListOf(1, 2).bind() * 10 } == lazyListOf(10, 20))
```

`LazyList.fx` uses `fxReplay` (multi-shot), like `ListK`. `bind` skips empty inner lists iteratively, so
filtering-style binds over long or infinite lists do not overflow the stack.

## Chain

A catenable sequence stored as a tree of appended segments, flattened only when iterated. `+`, `prepend`,
`append`, `size` and wrapping an existing list are O(1), which makes `Chain` a good accumulator for logs and
writer output. Iteration and `uncons` never recurse, so even very deeply nested chains are stack safe.
`equals`, `hashCode` and `toString` depend only on the elements, not on the internal shape.

```kotlin
import com.github.kotlinz.kotlinz.data.chain.Chain
import com.github.kotlinz.kotlinz.data.chain.chainOf
import com.github.kotlinz.kotlinz.data.chain.toChain
import com.github.kotlinz.kotlinz.data.maybe.Maybe

val chain = (Chain.one(1) + chainOf(2, 3)).prepend(0).append(4)

check(chain.toList() == listOf(0, 1, 2, 3, 4))
check(chain.size == 5)
check(chain.toString() == "Chain(0, 1, 2, 3, 4)")
check((chain bind { chainOf(it, it) }).size == 10)
check(chain.filter { it % 2 == 0 } == chainOf(0, 2, 4))
check(chain.collect { if (it > 2) "#$it" else null } == chainOf("#3", "#4"))
check((chainOf(1, 2, 3) zip chainOf("a", "b")) == chainOf(1 to "a", 2 to "b"))

val (head, rest) = (chain.uncons() as Maybe.Just).value
check(head == 0 && rest == chainOf(1, 2, 3, 4))

// A left-nested chain of 100k appends is still safe to iterate.
val big = (1..100_000).fold(Chain.empty<Int>()) { acc, n -> acc.append(n) }
check(big.foldl(0L) { s, n -> s + n } == 5_000_050_000L)

// Chain is a Monoid (concatenation); Chain.monoid() gives it as a first-class value.
val monoid = Chain.monoid<Int>()
check(monoid.combine(listOf(1).toChain(), monoid.empty) == chainOf(1))
```

## Tree

A rose tree: `Tree(root, children)`, built with `tree(root, vararg children)` and `Tree.leaf(value)`, or unfolded
from a seed with `Tree.unfoldTree`. Folds and traversals are pre-order (a node before its children). `flatten`
lists the nodes in pre-order, `levels` groups them breadth-first.

The `Applicative` / `Monad` follow Haskell's `Data.Tree`. As a `Comonad`, `duplicate` replaces every node with the
subtree rooted at it, so `extend` computes a value per node from its whole subtree. `Zip` pairs children by
index and drops subtrees that exist in only one tree; there is no `Align`, because a tree is never empty.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.SumMonoid
import com.github.kotlinz.kotlinz.data.monoid.toSumMonoid
import com.github.kotlinz.kotlinz.data.tree.Tree
import com.github.kotlinz.kotlinz.data.tree.tree

val t = tree(1, tree(2, Tree.leaf(4)), Tree.leaf(3))

check(t.flatten() == listOf(1, 2, 4, 3))
check(t.levels() == listOf(listOf(1), listOf(2, 3), listOf(4)))
check(t.size() == 4)
check((t fmap { it * 10 }).flatten() == listOf(10, 20, 40, 30))
check(t.reduceMap(SumMonoid::class.java) { it.toSumMonoid() } == SumMonoid(10))

// Comonad: the sum of every subtree.
check(t.extend { Tree.narrow(it).flatten().sum() } == tree(10, tree(6, Tree.leaf(4)), Tree.leaf(3)))

// Traversable1: only an Apply is needed (here the Maybe monad).
val doubled = t.traverse1(object : MaybeMonad {}) { n -> if (n > 0) Maybe.Just(n * 2) else Maybe.None() }
check(Maybe.narrow(doubled) fmap { Tree.narrow(it).root } == Maybe.Just(2))

val binary = Tree.unfoldTree(1) { n -> Pair(n, if (n < 4) listOf(n * 2, n * 2 + 1) else listOf()) }
check(binary.levels() == listOf(listOf(1), listOf(2, 3), listOf(4, 5, 6, 7)))
```

Operations recurse once per tree level, so extremely deep trees may exhaust the stack.

## NonEmptySet

A set with at least one element, backed by an insertion-ordered set, so `head` and the fold order are
deterministic. Values are built from at least one element (`nonEmptySetOf`) or checked at runtime
(`NonEmptySet.fromSet` / `Set.toNonEmptySetOrNull()`, returning `null` when empty). Union is a semigroup (not a
monoid, as there is no empty set): `+` or `NonEmptySet.semigroup()`.

There is deliberately no `Functor` instance (mapping can merge elements, as in Haskell's `Set`); use the plain
`map` helper. `Foldable1` gives reductions without an initial value.

```kotlin
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.nonemptyset.NonEmptySet
import com.github.kotlinz.kotlinz.data.nonemptyset.nonEmptySetOf
import com.github.kotlinz.kotlinz.data.nonemptyset.toNonEmptySet

val s = nonEmptySetOf(1, 2, 2, 3)

check(s.size == 3 && s.head == 1)
check(s + nonEmptySetOf(3, 4) == nonEmptySetOf(1, 2, 3, 4))
check(s.reduceLeft { a, b -> a + b } == 6)
check(s.map { it % 2 } == nonEmptySetOf(1, 0))
check(NonEmptySet.fromSet(emptySet<Int>()) == null)
check(nonEmptyListOf(1, 1, 2).toNonEmptySet().toNonEmptyList() == nonEmptyListOf(1, 2))
check(NonEmptySet.semigroup<Int>().combine(nonEmptySetOf(1), nonEmptySetOf(2)) == nonEmptySetOf(1, 2))
```

`NonEmptySet` (and `NonEmptyMap`) do not implement the `Semigroup` interface themselves, because the reflective
`Semigroup.op(type, ...)` helpers need a no-argument constructor that a non-empty type cannot have; the
`SemigroupOf` value is offered instead (see [Monoids](monoids.md)).

## NonEmptyMap

A map with at least one entry, backed by an insertion-ordered map. It is a `K2<NonEmptyMap.T, K, V>`, and the
instances act on the values with the keys fixed (`Functor<K1<NonEmptyMap.T, K>>`). `Semialign` takes the key
union; there is no `Zip` / `Align`, because a key intersection may be empty. `union` combines the values of
shared keys, keeping this map's keys first.

```kotlin
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.nonemptymap.NonEmptyMap
import com.github.kotlinz.kotlinz.data.nonemptymap.nonEmptyMapOf

val m = nonEmptyMapOf("a" to 1, "b" to 2)

check(m.head == ("a" to 1) && m["b"] == 2 && m.values == nonEmptyListOf(1, 2))
check((m fmap { it * 10 }) == nonEmptyMapOf("a" to 10, "b" to 20))
check(m.union(nonEmptyMapOf("b" to 5, "c" to 3)) { x, y -> x + y } == nonEmptyMapOf("a" to 1, "b" to 7, "c" to 3))
check(m.reduceLeft { x, y -> x + y } == 3)
check((m align nonEmptyMapOf("b" to "x")) == nonEmptyMapOf("a" to Ior.Left<Int, String>(1), "b" to Ior.Both(2, "x")))
check(NonEmptyMap.fromMap(emptyMap<String, Int>()) == null)

val checked = m.traverse1(object : MaybeMonad {}) { v -> if (v > 0) Maybe.Just(v.toString()) else Maybe.None() }
check(Maybe.narrow(checked) fmap { NonEmptyMap.narrow(it) } == Maybe.Just(nonEmptyMapOf("a" to "1", "b" to "2")))
```

`NonEmptyMap.semigroup(SemigroupOf<V>)` and `NonEmptyMap.semigroup(Class<V>)` give the union as a `SemigroupOf`.

## ListZipper

A non-empty list with a cursor: `ListZipper(lefts, focus, rights)`, with `lefts` and `rights` both stored in list
order (so `toList()` is `lefts + focus + rights`). `left()` / `right()` return `Maybe.None` at the ends, while
`moveTo`, `start` and `end` clamp. `modify` and `set` edit the focus.

As a `Comonad`, `duplicate` yields the zipper focused at every position, so `extend` is a natural fit for
context-dependent transformations such as moving averages or cellular automata.

```kotlin
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.nonemptylist.nonEmptyListOf
import com.github.kotlinz.kotlinz.data.zipper.ListZipper
import com.github.kotlinz.kotlinz.data.zipper.toListZipper

val z = ListZipper.of(1, 2, 3, 4)

check(z.focus == 1 && z.left() == Maybe.None<ListZipper<Int>>())
check((z.right() fmap { it.modify { x -> x * 10 }.toList() }) == Maybe.Just(listOf(1, 20, 3, 4)))
check(z.moveTo(2).focus == 3 && z.moveTo(99).focus == 4)
check(z.end().index == 3)

// Comonad: each position sees its neighbours.
val neighbourSums = z.extend { w ->
  val here = ListZipper.narrow(w)
  (here.lefts.lastOrNull() ?: 0) + here.focus + (here.rights.firstOrNull() ?: 0)
}
check(neighbourSums.toList() == listOf(3, 6, 9, 7))
check(neighbourSums.index == 0)

check(nonEmptyListOf(5, 6).toListZipper().reduceLeft { a, b -> a * b } == 30)
check(ListZipper.fromList(listOf<Int>()) == null)
```

Traversals run left to right over the whole list and restore the original focus position.

package com.github.kotlinz.kotlinz.data.zipper

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.applicative.Apply
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A non-empty list with a cursor: the [focus] element plus the elements to its left and right.
 *
 * [lefts] and [rights] are both stored in list order, so [toList] is `lefts + focus + rights`.
 * Moving the cursor with [left] / [right] returns [Maybe.None] at the ends. As a comonad,
 * [duplicate] yields the zipper focused at every position, which makes [extend] a natural fit for
 * context-dependent transformations such as moving averages or cellular automata.
 *
 * Instances: Functor, Foldable, Foldable1, Traversable, Traversable1, Copointed, Comonad.
 *
 * ```kotlin
 * val z = ListZipper.of(1, 2, 3)                 // focus on 1
 * z.right() fmap { it.modify { x -> x * 10 } }   // Just(ListZipper([1], 20, [3]))
 * z.extend { w -> ListZipper.narrow(w).toList().sum() } // every position sees the whole list
 * ```
 */
data class ListZipper<A>(val lefts: List<A>, val focus: A, val rights: List<A>): K1<ListZipper.T, A> {
  /** Witness type used to encode `ListZipper` as a higher-kinded `K1<ListZipper.T, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [ListZipper]. */
    fun <A> narrow(v: K1<T, A>): ListZipper<A> = v as ListZipper<A>

    /** A zipper focused on [head], with [tail] to its right. */
    fun <A> of(head: A, vararg tail: A): ListZipper<A> = ListZipper(listOf(), head, tail.toList())
    /** A zipper focused on the first element of [list], or `null` when it is empty. */
    fun <A> fromList(list: List<A>): ListZipper<A>? = list.firstOrNull()?.let { ListZipper(listOf(), it, list.drop(1)) }
    /** A zipper focused on the head of [list]. */
    fun <A> fromNonEmptyList(list: NonEmptyList<A>): ListZipper<A> = ListZipper(listOf(), list.head, list.tail)

    private val comonad = object: ListZipperComonad {}
    private val traversable = object: ListZipperTraversable1 {}
  }

  /** The position of the focus, counting from 0. */
  val index: Int get() = lefts.size
  /** The number of elements. */
  val size: Int get() = lefts.size + 1 + rights.size
  /** Moves the focus one step left, or returns [Maybe.None] at the first element. */
  fun left(): Maybe<ListZipper<A>> =
    lefts.lastOrNull()?.let { Maybe.Just(ListZipper(lefts.dropLast(1), it, listOf(focus) + rights)) } ?: Maybe.None()
  /** Moves the focus one step right, or returns [Maybe.None] at the last element. */
  fun right(): Maybe<ListZipper<A>> =
    rights.firstOrNull()?.let { Maybe.Just(ListZipper(lefts + focus, it, rights.drop(1))) } ?: Maybe.None()
  /** Moves the focus to the first element. */
  fun start(): ListZipper<A> = moveTo(0)
  /** Moves the focus to the last element. */
  fun end(): ListZipper<A> = moveTo(size - 1)
  /** Moves the focus to [position], clamped to the valid range. */
  fun moveTo(position: Int): ListZipper<A> = toList().let { all ->
    val i = position.coerceIn(0, all.size - 1)
    ListZipper(all.take(i), all[i], all.drop(i + 1))
  }
  /** Replaces the focus with `f(focus)`. */
  fun modify(f: (A) -> A): ListZipper<A> = copy(focus = f(focus))
  /** Replaces the focus with [value]. */
  fun set(value: A): ListZipper<A> = copy(focus = value)
  /** All elements in order. */
  fun toList(): List<A> = lefts + focus + rights
  /** All elements in order as a [NonEmptyList]. */
  fun toNonEmptyList(): NonEmptyList<A> = lefts.firstOrNull()?.let { NonEmptyList(it, lefts.drop(1) + focus + rights) } ?: NonEmptyList(focus, rights)

  // Functor
  /** Applies [f] to every element, keeping the focus position. */
  infix fun <B> fmap(f: (A) -> B): ListZipper<B> = comonad.fmap(f, this)

  // Comonad
  /** Returns the [focus]. */
  fun extract(): A = comonad.extract(this)
  /** The zipper focused at every position, with this zipper as the focus. */
  fun duplicate(): ListZipper<K1<T, A>> = comonad.duplicate(this)
  /** Applies [f] to the zipper focused at every position. */
  fun <B> extend(f: (K1<T, A>) -> B): ListZipper<B> = comonad.extend(f, this)

  // Foldable / Foldable1
  /** Folds all elements from the left. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = traversable.foldl(initial, f, this)
  /** Folds all elements from the right. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = traversable.foldr(initial, f, this)
  /** Maps every element into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = traversable.foldMap(type, f, this)
  /** Reduces all elements left to right without an initial value. */
  fun reduceLeft(f: (A, A) -> A): A = traversable.reduceLeft(f, this)
  /** Maps every element into the Semigroup [type] and combines the results. */
  fun <S: Semigroup<*, S>> reduceMap(type: Class<S>, f: (A) -> S): S = traversable.reduceMap(type, f, this)

  // Traversable / Traversable1
  /** Applies an effectful [f] to every element left to right, keeping the focus position. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  /** Like [traverse], but only needs an [Apply] because the zipper is never empty. */
  fun <G, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse1(apply, f, this)
}

/** Creates a [ListZipper] focused on the head of this [NonEmptyList]. */
fun <A> NonEmptyList<A>.toListZipper(): ListZipper<A> = ListZipper.fromNonEmptyList(this)

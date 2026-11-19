package com.github.kotlinz.kotlinz.data.nonemptyset

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.semigroupof.SemigroupOf
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A set that is guaranteed to contain at least one element.
 *
 * Elements keep their insertion order, so [head] and the fold order are deterministic. Values can
 * only be built from at least one element ([of]) or checked at runtime ([fromSet]), so reductions
 * such as [reduceLeft] never need an initial value. Union forms a semigroup (but not a monoid, as
 * there is no empty set), available as [semigroup] or the [plus] operator.
 *
 * Instances: Foldable, Foldable1.
 *
 * ```kotlin
 * val s = nonEmptySetOf(1, 2, 2, 3) // NonEmptySet(1, 2, 3)
 * s + nonEmptySetOf(3, 4)           // NonEmptySet(1, 2, 3, 4)
 * s.reduceLeft { a, b -> a + b }    // 6
 * NonEmptySet.fromSet(emptySet<Int>()) // null
 * ```
 */
class NonEmptySet<A> private constructor(private val elements: Set<A>): K1<NonEmptySet.T, A>, Iterable<A> {
  /** Witness type used to encode `NonEmptySet` as a higher-kinded `K1<NonEmptySet.T, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [NonEmptySet]. */
    fun <A> narrow(v: K1<T, A>): NonEmptySet<A> = v as NonEmptySet<A>

    /** Creates a set from [head] and further [tail] elements. */
    fun <A> of(head: A, vararg tail: A): NonEmptySet<A> = NonEmptySet(linkedSetOf(head, *tail))
    /** Converts a [Set] to a [NonEmptySet], returning `null` when the set is empty. */
    fun <A> fromSet(set: Set<A>): NonEmptySet<A>? = if (set.isEmpty()) null else NonEmptySet(LinkedHashSet(set))
    /** The union semigroup as a first-class dictionary. */
    fun <A> semigroup(): SemigroupOf<NonEmptySet<A>> = SemigroupOf { a, b -> a + b }

    private val foldable1 = object: NonEmptySetFoldable1 {}
  }

  /** The first element in insertion order. */
  val head: A get() = elements.first()
  /** The number of elements. */
  val size: Int get() = elements.size
  /** Returns true when [value] is an element of this set. */
  operator fun contains(value: A): Boolean = value in elements
  /** The union of this set and [other], keeping this set's elements first. */
  operator fun plus(other: NonEmptySet<A>): NonEmptySet<A> = NonEmptySet(elements + other.elements)
  /** This set with [value] added. */
  operator fun plus(value: A): NonEmptySet<A> = NonEmptySet(elements + value)
  /** This set with all [values] added. */
  fun plusAll(values: Iterable<A>): NonEmptySet<A> = NonEmptySet(elements + values)
  /** Applies [f] to every element; equal results are merged. */
  fun <B> map(f: (A) -> B): NonEmptySet<B> = NonEmptySet(elements.mapTo(LinkedHashSet(), f))
  /** Converts to a plain [Set]. */
  fun toSet(): Set<A> = elements
  /** Converts to a [NonEmptyList] in insertion order. */
  fun toNonEmptyList(): NonEmptyList<A> = NonEmptyList(head, elements.drop(1))
  override fun iterator(): Iterator<A> = elements.iterator()

  // Foldable
  /** Folds the elements from the left in insertion order. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable1.foldl(initial, f, this)
  /** Folds the elements from the right in insertion order. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable1.foldr(initial, f, this)
  /** Maps every element into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable1.foldMap(type, f, this)

  // Foldable1
  /** Reduces all elements left to right without an initial value. */
  fun reduceLeft(f: (A, A) -> A): A = foldable1.reduceLeft(f, this)
  /** Reduces all elements right to left without an initial value. */
  fun reduceRight(f: (A, A) -> A): A = foldable1.reduceRight(f, this)
  /** Maps every element into the Semigroup [type] and combines the results. */
  fun <S: Semigroup<*, S>> reduceMap(type: Class<S>, f: (A) -> S): S = foldable1.reduceMap(type, f, this)

  override fun equals(other: Any?): Boolean = other is NonEmptySet<*> && elements == other.elements
  override fun hashCode(): Int = elements.hashCode()
  override fun toString(): String = elements.joinToString(", ", "NonEmptySet(", ")")
}

/** Creates a [NonEmptySet] from [head] and further [tail] elements. */
fun <A> nonEmptySetOf(head: A, vararg tail: A): NonEmptySet<A> = NonEmptySet.of(head, *tail)
/** Converts this [Set] to a [NonEmptySet], or `null` when it is empty. */
fun <A> Set<A>.toNonEmptySetOrNull(): NonEmptySet<A>? = NonEmptySet.fromSet(this)
/** Converts this [NonEmptyList] to a [NonEmptySet], dropping duplicates. */
fun <A> NonEmptyList<A>.toNonEmptySet(): NonEmptySet<A> = NonEmptySet.of(head).plusAll(tail)

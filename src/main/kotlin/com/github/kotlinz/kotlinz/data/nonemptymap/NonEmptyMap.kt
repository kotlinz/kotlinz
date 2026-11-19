package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.semigroupof.SemigroupOf
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.applicative.Apply
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A map that is guaranteed to contain at least one entry.
 *
 * Entries keep their insertion order, which is also the order of folds and traversals. The type
 * class instances act on the values with the keys fixed (`NonEmptyMap<K, V>` is a
 * `Functor<K1<NonEmptyMap.T, K>>`). Union combines the values of shared keys with a value
 * semigroup, see [union] and [semigroup].
 *
 * Instances: Functor, Foldable, Foldable1, Traversable, Traversable1, Semialign.
 *
 * ```kotlin
 * val m = nonEmptyMapOf("a" to 1, "b" to 2)
 * m fmap { it * 10 }                                  // NonEmptyMap(a=10, b=20)
 * m.union(nonEmptyMapOf("b" to 5)) { x, y -> x + y } // NonEmptyMap(a=1, b=7)
 * NonEmptyMap.fromMap(emptyMap<String, Int>())       // null
 * ```
 */
class NonEmptyMap<K, V> private constructor(private val entries: Map<K, V>): K2<NonEmptyMap.T, K, V> {
  /** Witness type used to encode `NonEmptyMap` as a higher-kinded `K2<NonEmptyMap.T, K, V>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [NonEmptyMap]. */
    fun <K, V> narrow(v: K1<K1<T, K>, V>): NonEmptyMap<K, V> = v as NonEmptyMap<K, V>
    /** Wraps [entries], which the caller guarantees to be non-empty. */
    internal fun <K, V> wrap(entries: Map<K, V>): NonEmptyMap<K, V> = NonEmptyMap(entries)

    /** Creates a map from [head] and further [tail] entries; later entries win on duplicate keys. */
    fun <K, V> of(head: Pair<K, V>, vararg tail: Pair<K, V>): NonEmptyMap<K, V> = NonEmptyMap(linkedMapOf(head, *tail))
    /** Converts a [Map] to a [NonEmptyMap], returning `null` when the map is empty. */
    fun <K, V> fromMap(map: Map<K, V>): NonEmptyMap<K, V>? = if (map.isEmpty()) null else NonEmptyMap(LinkedHashMap(map))
    /** The union semigroup, combining the values of shared keys with [values]. */
    fun <K, V> semigroup(values: SemigroupOf<V>): SemigroupOf<NonEmptyMap<K, V>> = SemigroupOf { a, b -> a.union(b, values.combine) }
    /** The union semigroup, combining the values of shared keys with the Semigroup class [type]. */
    fun <K, V: Semigroup<*, V>> semigroup(type: Class<V>): SemigroupOf<NonEmptyMap<K, V>> = semigroup(SemigroupOf.from(type))

    // Traversable1
    /** Turns a map of effects into an effect of a map, using only [Apply]. */
    fun <G, K, V> sequence1(apply: Apply<G>, v: K1<K1<T, K>, K1<G, V>>): K1<G, K1<K1<T, K>, V>> = traversable<K>().sequence1(apply, v)

    private fun <K> traversable() = object: NonEmptyMapTraversable1<K> {}
    private fun <K> semialign() = object: NonEmptyMapSemialign<K> {}
  }

  /** The first entry in insertion order. */
  val head: Pair<K, V> get() = entries.entries.first().toPair()
  /** The number of entries. */
  val size: Int get() = entries.size
  /** The keys in insertion order. */
  val keys: Set<K> get() = entries.keys
  /** The values in insertion order. */
  val values: NonEmptyList<V> get() = NonEmptyList(head.second, entries.values.drop(1))
  /** Returns the value for [key], or `null` when absent. */
  operator fun get(key: K): V? = entries[key]
  /** Returns true when [key] has an entry. */
  fun containsKey(key: K): Boolean = key in entries
  /** This map with [entry] added or replaced. */
  operator fun plus(entry: Pair<K, V>): NonEmptyMap<K, V> = NonEmptyMap(entries + entry)
  /** The union with [other], combining the values of shared keys with [combine] (this map's value first). */
  fun union(other: NonEmptyMap<K, V>, combine: (V, V) -> V): NonEmptyMap<K, V> =
    NonEmptyMap(other.entries.entries.fold(LinkedHashMap(entries)) { acc, (k, v) ->
      acc.apply { put(k, if (containsKey(k)) combine(getValue(k), v) else v) }
    })
  /** Converts to a plain [Map]. */
  fun toMap(): Map<K, V> = entries

  // Functor
  /** Applies [f] to every value. */
  infix fun <W> fmap(f: (V) -> W): NonEmptyMap<K, W> = traversable<K>().fmap(f, this)

  // Foldable / Foldable1
  /** Folds the values from the left in insertion order. */
  fun <B> foldl(initial: B, f: (B, V) -> B): B = traversable<K>().foldl(initial, f, this)
  /** Folds the values from the right in insertion order. */
  fun <B> foldr(initial: B, f: (V, B) -> B): B = traversable<K>().foldr(initial, f, this)
  /** Maps every value into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (V) -> M): M = traversable<K>().foldMap(type, f, this)
  /** Reduces all values left to right without an initial value. */
  fun reduceLeft(f: (V, V) -> V): V = traversable<K>().reduceLeft(f, this)
  /** Maps every value into the Semigroup [type] and combines the results. */
  fun <S: Semigroup<*, S>> reduceMap(type: Class<S>, f: (V) -> S): S = traversable<K>().reduceMap(type, f, this)

  // Traversable / Traversable1
  /** Applies an effectful [f] to every value in insertion order and collects the results inside [applicative]. */
  fun <G, W> traverse(applicative: Applicative<G>, f: (V) -> K1<G, W>): K1<G, K1<K1<T, K>, W>> = traversable<K>().traverse(applicative, f, this)
  /** Like [traverse], but only needs an [Apply] because the map is never empty. */
  fun <G, W> traverse1(apply: Apply<G>, f: (V) -> K1<G, W>): K1<G, K1<K1<T, K>, W>> = traversable<K>().traverse1(apply, f, this)

  // Semialign
  /** Pairs the values of shared keys and keeps the entries present in only one map. */
  infix fun <W> align(m: K1<K1<T, K>, W>): NonEmptyMap<K, Ior<V, W>> = semialign<K>().align(this, m)
  /** Combines the values with [f], keeping the entries present in only one map. */
  fun <W, X> alignWith(m: K1<K1<T, K>, W>, f: (Ior<V, W>) -> X): NonEmptyMap<K, X> = semialign<K>().alignWith(f, this, m)

  override fun equals(other: Any?): Boolean = other is NonEmptyMap<*, *> && entries == other.entries
  override fun hashCode(): Int = entries.hashCode()
  override fun toString(): String = entries.entries.joinToString(", ", "NonEmptyMap(", ")")
}

/** Creates a [NonEmptyMap] from [head] and further [tail] entries. */
fun <K, V> nonEmptyMapOf(head: Pair<K, V>, vararg tail: Pair<K, V>): NonEmptyMap<K, V> = NonEmptyMap.of(head, *tail)
/** Converts this [Map] to a [NonEmptyMap], or `null` when it is empty. */
fun <K, V> Map<K, V>.toNonEmptyMapOrNull(): NonEmptyMap<K, V>? = NonEmptyMap.fromMap(this)

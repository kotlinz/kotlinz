package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monus

/**
 * Multisets (bags) under sum, with multiset difference as [Monus].
 *
 * [counts] maps each element to its multiplicity; only positive counts are stored, so equal multisets
 * are equal maps. [op] adds multiplicities and [monus] subtracts them, dropping elements that reach zero.
 *
 * ```kotlin
 * val clazz = MultisetMonoid::class.java as Class<MultisetMonoid<String>>
 * Monus.monus(clazz, listOf("a", "a", "b").toMultisetMonoid(), listOf("a", "c").toMultisetMonoid())
 * // MultisetMonoid({a=1, b=1})
 * ```
 */
data class MultisetMonoid<A>(val counts: Map<A, Int>) : Monus<Map<A, Int>, MultisetMonoid<A>> {
    init {
        require(counts.values.all { it > 0 }) { "MultisetMonoid requires positive counts but was $counts" }
    }

    /** Creates the empty multiset; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(mapOf())

    override fun op(a: MultisetMonoid<A>, b: MultisetMonoid<A>) =
        MultisetMonoid((a.counts.keys + b.counts.keys).associateWith { a.count(it) + b.count(it) })
    override fun mzero() = MultisetMonoid<A>(mapOf())
    override fun monus(a: MultisetMonoid<A>, b: MultisetMonoid<A>) =
        MultisetMonoid(a.counts.mapValues { (k, n) -> n - b.count(k) }.filterValues { it > 0 })

    /** Returns the multiplicity of [element] (zero when absent). */
    fun count(element: A): Int = counts[element] ?: 0
}

/** Builds a [MultisetMonoid] counting the elements of this list. */
fun <A> List<A>.toMultisetMonoid() = MultisetMonoid(groupingBy { it }.eachCount())

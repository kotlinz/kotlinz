package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.monoid.toListMonoid
import com.github.kotlinz.kotlinz.type.Foldable
import io.kotest.matchers.booleans.shouldBeTrue

class FoldableLaw<T>(val foldable: Foldable<T>) {
    fun <V> foldlConsistency(v: K1<T, V>): Boolean =
            foldable.foldl(listOf<V>(), { acc, a -> acc + a }, v) == foldable.toList(v)

    fun <V> foldrConsistency(v: K1<T, V>): Boolean =
            foldable.foldr(listOf<V>(), { a, acc -> listOf(a) + acc }, v) == foldable.toList(v)

    fun <V> foldMapConsistency(v: K1<T, V>): Boolean =
            foldable.foldMap(ListMonoid::class.java, { listOf(it).toListMonoid() }, v).value == foldable.toList(v)

    fun <V> lengthConsistency(v: K1<T, V>): Boolean =
            foldable.length(v) == foldable.toList(v).size && foldable.isEmpty(v) == foldable.toList(v).isEmpty()

    fun <V> assertSatisfyingFoldableLaw(v: K1<T, V>) {
        foldlConsistency(v).shouldBeTrue()
        foldrConsistency(v).shouldBeTrue()
        foldMapConsistency(v).shouldBeTrue()
        lengthConsistency(v).shouldBeTrue()
    }
}

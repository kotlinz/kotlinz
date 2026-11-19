package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.type.Foldable1
import io.kotest.matchers.booleans.shouldBeTrue

class Foldable1Law<T>(val foldable: Foldable1<T>) {
    fun <V> nonEmptiness(v: K1<T, V>): Boolean = foldable.toList(v).isNotEmpty() && !foldable.isEmpty(v)

    fun <V> reduceConsistency(v: K1<T, V>, f: (V, V) -> V): Boolean =
            foldable.reduceLeft(f, v) == foldable.toList(v).reduce(f) &&
                    foldable.reduceRight(f, v) == foldable.toList(v).reduceRight(f)

    fun <V> reduceMapConsistency(v: K1<T, V>): Boolean {
        val type = StringMonoid::class.java
        val f = { a: V -> StringMonoid("<$a>") }
        return foldable.reduceMap(type, f, v) == foldable.foldMap(type, f, v)
    }

    fun <V> endsConsistency(v: K1<T, V>): Boolean =
            foldable.head(v) == foldable.toList(v).first() && foldable.last(v) == foldable.toList(v).last()

    fun <V> assertSatisfyingFoldable1Law(v: K1<T, V>, f: (V, V) -> V) {
        nonEmptiness(v).shouldBeTrue()
        reduceConsistency(v, f).shouldBeTrue()
        reduceMapConsistency(v).shouldBeTrue()
        endsConsistency(v).shouldBeTrue()
    }
}

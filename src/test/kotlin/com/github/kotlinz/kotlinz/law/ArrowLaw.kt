package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow
import io.kotest.matchers.booleans.shouldBeTrue

/** Category and Arrow laws, checked observationally through [observe] on small integer inputs. */
class ArrowLaw<T, F>(val arrow: Arrow<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()
    private val pairs = ints.flatMap { a -> ints.map { c -> Pair(a, c) } }

    fun leftIdentity(v: K3<T, F, Int, Int>): Boolean = same(arrow.compose(arrow.id(), v), v, ints)

    fun rightIdentity(v: K3<T, F, Int, Int>): Boolean = same(arrow.compose(v, arrow.id()), v, ints)

    fun associativity(u: K3<T, F, Int, Int>, v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>): Boolean =
            same(arrow.compose(u, arrow.compose(v, w)), arrow.compose(arrow.compose(u, v), w), ints)

    fun arrIdentity(): Boolean = same(arrow.arr { a: Int -> a }, arrow.id<Int>(), ints)

    fun arrComposition(f: (Int) -> Int, g: (Int) -> Int): Boolean =
            same(arrow.arr { a: Int -> g(f(a)) }, arrow.compose(arrow.arr(g), arrow.arr(f)), ints)

    fun firstArr(f: (Int) -> Int): Boolean =
            same(arrow.first<Int, Int, Int>(arrow.arr(f)), arrow.arr { p: Pair<Int, Int> -> Pair(f(p.first), p.second) }, pairs)

    fun firstComposition(v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>): Boolean =
            same(arrow.first<Int, Int, Int>(arrow.compose(w, v)), arrow.compose(arrow.first<Int, Int, Int>(w), arrow.first<Int, Int, Int>(v)), pairs)

    fun firstDiscards(v: K3<T, F, Int, Int>): Boolean {
        val fst = { p: Pair<Int, Int> -> p.first }
        return same(arrow.compose(arrow.arr(fst), arrow.first<Int, Int, Int>(v)), arrow.compose(v, arrow.arr(fst)), pairs)
    }

    fun assertSatisfyingCategoryLaw(u: K3<T, F, Int, Int>, v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>) {
        leftIdentity(v).shouldBeTrue()
        rightIdentity(v).shouldBeTrue()
        associativity(u, v, w).shouldBeTrue()
    }

    fun assertSatisfyingArrowLaw(v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>) {
        assertSatisfyingCategoryLaw(v, w, v)
        arrIdentity().shouldBeTrue()
        arrComposition({ it + 1 }, { it * 3 }).shouldBeTrue()
        firstArr { it - 2 }.shouldBeTrue()
        firstComposition(v, w).shouldBeTrue()
        firstDiscards(v).shouldBeTrue()
    }

    private fun <X> same(a: K3<T, F, X, *>, b: K3<T, F, X, *>, inputs: List<X>): Boolean =
            inputs.all { observe(a)(it) == observe(b)(it) }
}

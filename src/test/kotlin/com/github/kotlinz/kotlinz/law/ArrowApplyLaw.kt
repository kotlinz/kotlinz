package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.ArrowApply
import io.kotest.matchers.booleans.shouldBeTrue

class ArrowApplyLaw<T, F>(val arrow: ArrowApply<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()

    fun identity(): Boolean {
        val curried = arrow.arr { x: Int -> arrow.arr { y: Int -> Pair(x, y) } }
        val lhs = andThen(arrow.first<Int, K3<T, F, Int, Pair<Int, Int>>, Int>(curried), arrow.app())
        return same(lhs, arrow.id(), ints.flatMap { x -> ints.map { y -> Pair(x, y) } })
    }

    fun precomposition(arrows: List<K3<T, F, Int, Int>>, g: K3<T, F, Int, Int>): Boolean {
        val lhs = andThen(arrow.first<K3<T, F, Int, Int>, K3<T, F, Int, Int>, Int>(arrow.arr { h -> andThen(g, h) }), arrow.app())
        val rhs = andThen(arrow.second<Int, Int, K3<T, F, Int, Int>>(g), arrow.app())
        return same(lhs, rhs, inputsOf(arrows))
    }

    fun postcomposition(arrows: List<K3<T, F, Int, Int>>, h: K3<T, F, Int, Int>): Boolean {
        val lhs = andThen(arrow.first<K3<T, F, Int, Int>, K3<T, F, Int, Int>, Int>(arrow.arr { k -> andThen(k, h) }), arrow.app())
        val rhs = andThen(arrow.app<Int, Int>(), h)
        return same(lhs, rhs, inputsOf(arrows))
    }

    fun assertSatisfyingArrowApplyLaw(v: K3<T, F, Int, Int>, w: K3<T, F, Int, Int>) {
        identity().shouldBeTrue()
        precomposition(listOf(v, w), w).shouldBeTrue()
        postcomposition(listOf(v, w), v).shouldBeTrue()
    }

    private fun inputsOf(arrows: List<K3<T, F, Int, Int>>) = arrows.flatMap { k -> ints.map { a -> Pair(k, a) } }

    private fun <A, B, C> andThen(f: K3<T, F, A, B>, g: K3<T, F, B, C>): K3<T, F, A, C> = arrow.compose(g, f)

    private fun <X> same(a: K3<T, F, X, *>, b: K3<T, F, X, *>, inputs: List<X>): Boolean =
            inputs.all { observe(a)(it) == observe(b)(it) }
}

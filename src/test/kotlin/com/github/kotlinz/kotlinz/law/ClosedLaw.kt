package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Closed
import io.kotest.matchers.booleans.shouldBeTrue

/**
 * Closed laws. [observe] runs a profunctor value on an input; [asFunction] turns an observed
 * function-valued output (possibly wrapped, e.g. in `Identity`) into a plain function.
 */
class ClosedLaw<T, F>(
        val closed: Closed<T, F>,
        val observe: (K3<T, F, *, *>) -> (Any?) -> Any?,
        val asFunction: (Any?) -> (Any?) -> Any?
) {
    private val ints = (-3..3).toList()
    private val functions = listOf({ x: Int -> x + 1 }, { x: Int -> x * x })

    fun closedCommutes(v: K3<T, F, Int, Int>, f: (Int) -> Int): Boolean {
        val pre = { g: (Int) -> Int -> { x: Int -> g(f(x)) } }
        val left = closed.lmap(pre, closed.closed<Int, Int, Int>(v))
        val right = closed.rmap(pre, closed.closed<Int, Int, Int>(v))
        return functions.all { g -> ints.all { x -> asFunction(observe(left)(g))(x) == asFunction(observe(right)(g))(x) } }
    }

    fun closedConstant(v: K3<T, F, Int, Int>): Boolean {
        val constant = closed.dimap({ a: Int -> { _: Unit -> a } }, { g: (Unit) -> Int -> g(Unit) }, closed.closed<Int, Int, Unit>(v))
        return ints.all { observe(constant)(it) == observe(v)(it) }
    }

    fun closedNests(v: K3<T, F, Int, Int>): Boolean {
        val nested = closed.closed<(Int) -> Int, (Int) -> Int, Int>(closed.closed<Int, Int, Int>(v))
        val flat = closed.dimap(::uncurry, ::curry, closed.closed<Int, Int, Pair<Int, Int>>(v))
        val curried = functions.map { g -> { x: Int -> { y: Int -> g(x + y) } } }
        return curried.all { g -> ints.all { x -> ints.all { y -> applyNested(nested, g, x, y) == applyNested(flat, g, x, y) } } }
    }

    fun assertSatisfyingClosedLaw(v: K3<T, F, Int, Int>) {
        closedCommutes(v) { it * 3 }.shouldBeTrue()
        closedConstant(v).shouldBeTrue()
        closedNests(v).shouldBeTrue()
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyNested(p: K3<T, F, *, *>, g: (Int) -> (Int) -> Int, x: Int, y: Int): Any? =
            (asFunction(observe(p)(g))(x) as (Any?) -> Any?)(y)
}

private fun <X, Y, A> uncurry(g: (X) -> (Y) -> A): (Pair<X, Y>) -> A = { p -> g(p.first)(p.second) }
private fun <X, Y, B> curry(g: (Pair<X, Y>) -> B): (X) -> (Y) -> B = { x -> { y -> g(Pair(x, y)) } }

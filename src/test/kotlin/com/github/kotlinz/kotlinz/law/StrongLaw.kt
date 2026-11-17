package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong
import io.kotest.matchers.booleans.shouldBeTrue

class StrongLaw<T, F>(val strong: Strong<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()
    private val pairs = ints.flatMap { a -> ints.map { c -> Pair(a, c) } }

    fun firstDiscards(v: K3<T, F, Int, Int>): Boolean =
            same(strong.lmap({ p: Pair<Int, Int> -> p.first }, v), strong.rmap({ p: Pair<Int, Int> -> p.first }, strong.first<Int, Int, Int>(v)), pairs)

    fun firstCommutes(v: K3<T, F, Int, Int>, f: (Int) -> Int): Boolean =
            same(
                    strong.lmap({ p: Pair<Int, Int> -> Pair(p.first, f(p.second)) }, strong.first<Int, Int, Int>(v)),
                    strong.rmap({ p: Pair<Int, Int> -> Pair(p.first, f(p.second)) }, strong.first<Int, Int, Int>(v)),
                    pairs
            )

    fun firstAssociates(v: K3<T, F, Int, Int>): Boolean =
            same(
                    strong.first<Pair<Int, Int>, Pair<Int, Int>, Int>(strong.first<Int, Int, Int>(v)),
                    strong.dimap(::assoc, ::unassoc, strong.first<Int, Int, Pair<Int, Int>>(v)),
                    pairs.flatMap { p -> ints.map { d -> Pair(p, d) } }
            )

    fun secondMirrorsFirst(v: K3<T, F, Int, Int>): Boolean =
            same(strong.second<Int, Int, Int>(v), strong.dimap(::swap, ::swap, strong.first<Int, Int, Int>(v)), pairs)

    fun assertSatisfyingStrongLaw(v: K3<T, F, Int, Int>) {
        firstDiscards(v).shouldBeTrue()
        firstCommutes(v) { it * 7 }.shouldBeTrue()
        firstAssociates(v).shouldBeTrue()
        secondMirrorsFirst(v).shouldBeTrue()
    }

    private fun <X> same(a: K3<T, F, X, *>, b: K3<T, F, X, *>, inputs: List<X>): Boolean =
            inputs.all { observe(a)(it) == observe(b)(it) }
}

private fun <A, C, D> assoc(p: Pair<Pair<A, C>, D>): Pair<A, Pair<C, D>> = Pair(p.first.first, Pair(p.first.second, p.second))
private fun <B, C, D> unassoc(p: Pair<B, Pair<C, D>>): Pair<Pair<B, C>, D> = Pair(Pair(p.first, p.second.first), p.second.second)
private fun <X, Y> swap(p: Pair<X, Y>): Pair<Y, X> = Pair(p.second, p.first)

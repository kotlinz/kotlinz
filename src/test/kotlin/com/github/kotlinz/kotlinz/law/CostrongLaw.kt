package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Costrong
import io.kotest.matchers.booleans.shouldBeTrue

class CostrongLaw<T, F>(val costrong: Costrong<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()

    fun unsecondMirrorsUnfirst(v: K3<T, F, Pair<Int, Int>, Pair<Int, Int>>): Boolean =
            same(
                    costrong.unsecond<Int, Int, Int>(v),
                    costrong.unfirst<Int, Int, Int>(costrong.dimap(::swapPair, ::swapPair, v))
            )

    fun unfirstAssociates(v: K3<T, F, Pair<Pair<Int, Int>, Int>, Pair<Pair<Int, Int>, Int>>): Boolean =
            same(
                    costrong.unfirst<Int, Int, Int>(costrong.unfirst<Pair<Int, Int>, Pair<Int, Int>, Int>(v)),
                    costrong.unfirst<Int, Int, Pair<Int, Int>>(costrong.dimap(::unassocPair, ::assocPair, v))
            )

    fun assertSatisfyingCostrongLaw(
            v: K3<T, F, Pair<Int, Int>, Pair<Int, Int>>,
            w: K3<T, F, Pair<Pair<Int, Int>, Int>, Pair<Pair<Int, Int>, Int>>
    ) {
        unsecondMirrorsUnfirst(v).shouldBeTrue()
        unfirstAssociates(w).shouldBeTrue()
    }

    private fun same(a: K3<T, F, Int, *>, b: K3<T, F, Int, *>): Boolean = ints.all { observe(a)(it) == observe(b)(it) }
}

private fun <X, Y> swapPair(p: Pair<X, Y>): Pair<Y, X> = Pair(p.second, p.first)
private fun <A, C, D> assocPair(p: Pair<Pair<A, C>, D>): Pair<A, Pair<C, D>> = Pair(p.first.first, Pair(p.first.second, p.second))
private fun <B, C, D> unassocPair(p: Pair<B, Pair<C, D>>): Pair<Pair<B, C>, D> = Pair(Pair(p.first, p.second.first), p.second.second)

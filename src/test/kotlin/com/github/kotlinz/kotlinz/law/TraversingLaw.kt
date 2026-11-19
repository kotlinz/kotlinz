package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander
import io.kotest.matchers.booleans.shouldBeTrue

class TraversingLaw<T, F>(val traversing: Traversing<T, F>, val observe: (K3<T, F, *, *>) -> (Any?) -> Any?) {
    private val ints = (-3..3).toList()
    private val lists = listOf(listOf(), listOf(Pair(1, 2)), listOf(Pair(-3, 4), Pair(0, 2), Pair(5, 6)))

    fun wanderIdentity(v: K3<T, F, Int, Int>): Boolean =
            ints.all { observe(traversing.wander(identityWander<Int, Int>(), v))(it) == observe(v)(it) }

    fun wanderComposes(v: K3<T, F, Int, Int>): Boolean {
        val nested = traversing.wander(listWander<Pair<Int, Int>, Pair<Int, Int>>(), traversing.wander(pairWander<Int, Int>(), v))
        val composed = traversing.wander(composeWander(listWander(), pairWander<Int, Int>()), v)
        return lists.all { observe(nested)(it) == observe(composed)(it) }
    }

    fun assertSatisfyingTraversingLaw(v: K3<T, F, Int, Int>) {
        wanderIdentity(v).shouldBeTrue()
        wanderComposes(v).shouldBeTrue()
    }
}

fun <A, B> identityWander() = object : Wander<A, B, A, B> {
    override fun <G> run(applicative: Applicative<G>, f: (A) -> K1<G, B>, s: A): K1<G, B> = f(s)
}

fun <A, B> pairWander() = object : Wander<Pair<A, A>, Pair<B, B>, A, B> {
    override fun <G> run(applicative: Applicative<G>, f: (A) -> K1<G, B>, s: Pair<A, A>): K1<G, Pair<B, B>> =
            applicative.ap(applicative.fmap({ b1: B -> { b2: B -> Pair(b1, b2) } }, f(s.first)), f(s.second))
}

fun <A, B> listWander() = object : Wander<List<A>, List<B>, A, B> {
    override fun <G> run(applicative: Applicative<G>, f: (A) -> K1<G, B>, s: List<A>): K1<G, List<B>> =
            s.foldRight(applicative.pure(listOf())) { a, acc ->
                applicative.ap(applicative.fmap({ b: B -> { bs: List<B> -> listOf(b) + bs } }, f(a)), acc)
            }
}

fun <S, T, A, B, C, D> composeWander(outer: Wander<S, T, A, B>, inner: Wander<A, B, C, D>) = object : Wander<S, T, C, D> {
    override fun <G> run(applicative: Applicative<G>, f: (C) -> K1<G, D>, s: S): K1<G, T> =
            outer.run(applicative, { a: A -> inner.run(applicative, f, a) }, s)
}

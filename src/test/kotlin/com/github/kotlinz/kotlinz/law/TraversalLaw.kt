package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.optics.Traversal
import com.github.kotlinz.kotlinz.optics.modify
import com.github.kotlinz.kotlinz.optics.traverseOf
import io.kotest.matchers.booleans.shouldBeTrue

class TraversalLaw<S, A>(val traversal: Traversal<S, S, A, A>) {
    private val maybe = object : MaybeApplicative {}
    private val listK = object : ListKApplicative {}

    fun identity(s: S): Boolean = traversal.traverseOf(object : IdentityMonad {}, { a: A -> Identity(a) }, s) == Identity(s)

    fun modifyIdentity(s: S): Boolean = traversal.modify { a: A -> a }(s) == s

    fun composition(s: S, f: (A) -> K1<Maybe.T, A>, g: (A) -> K1<ListK.T, A>): Boolean {
        val left = traversal.traverseOf(NestedApplicative(maybe, listK), { a: A -> Nested(maybe.fmap(g, f(a))) }, s)
        val right = Nested(maybe.fmap({ t: S -> traversal.traverseOf(listK, g, t) }, traversal.traverseOf(maybe, f, s)))
        return left == right
    }

    fun modifyComposition(s: S, f: (A) -> A, g: (A) -> A): Boolean =
            traversal.modify(f)(traversal.modify(g)(s)) == traversal.modify { a: A -> f(g(a)) }(s)

    fun assertSatisfyingTraversalLaw(ss: List<S>, f: (A) -> A, g: (A) -> A) {
        val fm = { a: A -> Maybe.Just(f(a)) }
        val gl = { a: A -> ListK(listOf(g(a), a)) }
        ss.all(::identity).shouldBeTrue()
        ss.all(::modifyIdentity).shouldBeTrue()
        ss.all { composition(it, fm, gl) }.shouldBeTrue()
        ss.all { modifyComposition(it, f, g) }.shouldBeTrue()
    }
}

package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.type.Traversable
import io.kotest.matchers.booleans.shouldBeTrue

class TraversableLaw<T>(val traversable: Traversable<T>) {
    private val identity = object : IdentityMonad {}
    private val maybe = object : MaybeApplicative {}
    private val listK = object : ListKApplicative {}

    fun <V> identity(v: K1<T, V>): Boolean =
            traversable.traverse(identity, { a: V -> Identity(a) }, v) == Identity(v)

    fun <V> naturality(v: K1<T, V>, f: (V) -> K1<Maybe.T, V>): Boolean =
            maybeToListK(traversable.traverse(maybe, f, v)) == traversable.traverse(listK, { a: V -> maybeToListK(f(a)) }, v)

    fun <V> composition(v: K1<T, V>, f: (V) -> K1<Maybe.T, V>, g: (V) -> K1<ListK.T, V>): Boolean {
        val nested = NestedApplicative(maybe, listK)
        val left = traversable.traverse(nested, { a: V -> Nested(maybe.fmap(g, f(a))) }, v)
        val right = Nested(maybe.fmap({ tb: K1<T, V> -> traversable.traverse(listK, g, tb) }, traversable.traverse(maybe, f, v)))
        return left == right
    }

    fun <V> assertSatisfyingTraversableLaw(v: K1<T, V>, f: (V) -> K1<Maybe.T, V>, g: (V) -> K1<ListK.T, V>) {
        identity(v).shouldBeTrue()
        naturality(v, f).shouldBeTrue()
        composition(v, f, g).shouldBeTrue()
    }

    private fun <A> maybeToListK(m: K1<Maybe.T, A>): ListK<A> = Maybe.narrow(m).foldr(ListK(listOf())) { a, acc -> ListK(listOf(a) + acc.value) }
}

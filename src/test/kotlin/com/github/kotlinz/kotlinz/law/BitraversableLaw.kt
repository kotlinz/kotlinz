package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.type.Bitraversable
import io.kotest.matchers.booleans.shouldBeTrue

class BitraversableLaw<T>(val bitraversable: Bitraversable<T>) {
    private val identity = object : IdentityMonad {}
    private val maybe = object : MaybeApplicative {}
    private val listK = object : ListKApplicative {}

    fun <A, B> identity(v: K2<T, A, B>): Boolean =
            bitraversable.bitraverse(identity, { a: A -> Identity(a) }, { b: B -> Identity(b) }, v) == Identity(v)

    fun <A, B> naturality(v: K2<T, A, B>, f: (A) -> K1<Maybe.T, A>, g: (B) -> K1<Maybe.T, B>): Boolean =
            maybeToListK(bitraversable.bitraverse(maybe, f, g, v)) ==
                    bitraversable.bitraverse(listK, { a: A -> maybeToListK(f(a)) }, { b: B -> maybeToListK(g(b)) }, v)

    fun <A, B> composition(v: K2<T, A, B>, f: (A) -> K1<Maybe.T, A>, g: (B) -> K1<Maybe.T, B>, h: (A) -> K1<ListK.T, A>, i: (B) -> K1<ListK.T, B>): Boolean {
        val nested = NestedApplicative(maybe, listK)
        val left = bitraversable.bitraverse(nested, { a: A -> Nested(maybe.fmap(h, f(a))) }, { b: B -> Nested(maybe.fmap(i, g(b))) }, v)
        val right = Nested(maybe.fmap({ t: K2<T, A, B> -> bitraversable.bitraverse(listK, h, i, t) }, bitraversable.bitraverse(maybe, f, g, v)))
        return left == right
    }

    fun <A, B> bimapConsistency(v: K2<T, A, B>, f: (A) -> A, g: (B) -> B): Boolean =
            bitraversable.bitraverse(identity, { a: A -> Identity(f(a)) }, { b: B -> Identity(g(b)) }, v) == Identity(bitraversable.bimap(f, g, v))

    fun <A, B> assertSatisfyingBitraversableLaw(v: K2<T, A, B>, f: (A) -> K1<Maybe.T, A>, g: (B) -> K1<Maybe.T, B>, h: (A) -> K1<ListK.T, A>, i: (B) -> K1<ListK.T, B>) {
        identity(v).shouldBeTrue()
        naturality(v, f, g).shouldBeTrue()
        composition(v, f, g, h, i).shouldBeTrue()
    }

    private fun <A> maybeToListK(m: K1<Maybe.T, A>): ListK<A> = Maybe.narrow(m).foldr(ListK(listOf())) { a, acc -> ListK(listOf(a) + acc.value) }
}

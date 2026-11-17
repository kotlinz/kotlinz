package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.adjunction.Adjunction
import io.kotest.matchers.booleans.shouldBeTrue

class AdjunctionLaw<F, G>(val adjunction: Adjunction<F, G>, val eq: (Any?, Any?) -> Boolean = { a, b -> a == b }) {
    fun <A> leftTriangle(fa: K1<F, A>): Boolean =
            eq(adjunction.counit(adjunction.left.fmap({ a: A -> adjunction.unit(a) }, fa)), fa)

    fun <A> rightTriangle(ga: K1<G, A>): Boolean =
            eq(adjunction.right.fmap({ fga: K1<F, K1<G, A>> -> adjunction.counit(fga) }, adjunction.unit(ga)), ga)

    fun <A, B> leftRightIdentity(a: A, f: (A) -> K1<G, B>): Boolean =
            eq(adjunction.leftAdjunct(adjunction.rightAdjunct(f))(a), f(a))

    fun <A, B> rightLeftIdentity(fa: K1<F, A>, f: (K1<F, A>) -> B): Boolean =
            eq(adjunction.rightAdjunct(adjunction.leftAdjunct(f))(fa), f(fa))

    fun <A, B> assertSatisfyingAdjunctionLaw(a: A, fa: K1<F, A>, ga: K1<G, A>, f: (A) -> K1<G, B>, g: (K1<F, A>) -> B) {
        leftTriangle(fa).shouldBeTrue()
        rightTriangle(ga).shouldBeTrue()
        leftRightIdentity(a, f).shouldBeTrue()
        rightLeftIdentity(fa, g).shouldBeTrue()
    }
}

package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityFunctor
import com.github.kotlinz.kotlinz.type.distributive.Distributive
import io.kotest.matchers.booleans.shouldBeTrue

class DistributiveLaw<G>(val distributive: Distributive<G>, val eq: (Any?, Any?) -> Boolean = { a, b -> a == b }) {
    private val identityFunctor = object : IdentityFunctor {}

    fun <A> identity(ga: K1<G, A>): Boolean =
            eq(distributive.distribute(identityFunctor, Identity(ga)), distributive.fmap({ a: A -> Identity(a) }, ga))

    fun <A> involution(gga: K1<G, K1<G, A>>): Boolean =
            eq(distributive.distribute(distributive, distributive.distribute(distributive, gga)), gga)

    fun <A, B> collectConsistency(f: (A) -> K1<G, B>, ga: K1<G, A>): Boolean =
            eq(distributive.collect(distributive, f, ga), distributive.distribute(distributive, distributive.fmap(f, ga)))

    fun <A, B> assertSatisfyingDistributiveLaw(ga: K1<G, A>, gga: K1<G, K1<G, A>>, f: (A) -> K1<G, B>) {
        identity(ga).shouldBeTrue()
        involution(gga).shouldBeTrue()
        collectConsistency(f, ga).shouldBeTrue()
    }
}

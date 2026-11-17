package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import io.kotest.matchers.booleans.shouldBeTrue

class ApplicativeLaw<T>(val applicative: Applicative<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A> identity(v: K1<T, A>): Boolean =
            eq(applicative.ap(applicative.pure { a: A -> a }, v), v)

    fun <A, B> homomorphism(a: A, f: (A) -> B): Boolean =
            eq(applicative.ap(applicative.pure(f), applicative.pure(a)), applicative.pure(f(a)))

    fun <A, B> interchange(u: K1<T, (A) -> B>, a: A): Boolean =
            eq(applicative.ap(u, applicative.pure(a)), applicative.ap(applicative.pure { f: (A) -> B -> f(a) }, u))

    fun <A, B, C> composition(u: K1<T, (B) -> C>, v: K1<T, (A) -> B>, w: K1<T, A>): Boolean {
        val compose = { g: (B) -> C -> { f: (A) -> B -> { a: A -> g(f(a)) } } }
        val left = applicative.ap(applicative.ap(applicative.ap(applicative.pure(compose), u), v), w)
        return eq(left, applicative.ap(u, applicative.ap(v, w)))
    }

    fun <A, B, C> assertSatisfyingApplicativeLaw(w: K1<T, A>, a: A, u: K1<T, (B) -> C>, v: K1<T, (A) -> B>, f: (A) -> B) {
        identity(w).shouldBeTrue()
        homomorphism(a, f).shouldBeTrue()
        interchange(v, a).shouldBeTrue()
        composition(u, v, w).shouldBeTrue()
    }
}

package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Filterable
import com.github.kotlinz.kotlinz.type.Witherable
import io.kotest.matchers.booleans.shouldBeTrue

class FilterableLaw<T>(val filterable: Filterable<T>) {
    fun <A> conservation(v: K1<T, A>, f: (A) -> A): Boolean =
            filterable.mapFilter({ a: A -> Maybe.Just(f(a)) }, v) == filterable.fmap(f, v)

    fun <A> composition(v: K1<T, A>, f: (A) -> Maybe<A>, g: (A) -> Maybe<A>): Boolean =
            filterable.mapFilter(g, filterable.mapFilter(f, v)) == filterable.mapFilter({ a: A -> Maybe.narrow(f(a) bind g) }, v)

    fun <A> filterConsistency(v: K1<T, A>, p: (A) -> Boolean): Boolean =
            filterable.filter(p, v) == filterable.mapFilter({ a: A -> if (p(a)) Maybe.Just(a) else Maybe.None() }, v)

    fun <A> assertSatisfyingFilterableLaw(v: K1<T, A>, f: (A) -> Maybe<A>, g: (A) -> Maybe<A>, p: (A) -> Boolean, h: (A) -> A) {
        conservation(v, h).shouldBeTrue()
        composition(v, f, g).shouldBeTrue()
        filterConsistency(v, p).shouldBeTrue()
    }
}

class WitherableLaw<T>(val witherable: Witherable<T>) {
    private val identity = object : IdentityMonad {}

    fun <A> identity(v: K1<T, A>, f: (A) -> Maybe<A>): Boolean =
            witherable.wither(identity, { a: A -> Identity(f(a)) }, v) == Identity(witherable.mapFilter(f, v))

    fun <A> filterAConsistency(v: K1<T, A>, p: (A) -> Boolean): Boolean =
            witherable.filterA(identity, { a: A -> Identity(p(a)) }, v) == Identity(witherable.filter(p, v))

    fun <A> assertSatisfyingWitherableLaw(v: K1<T, A>, f: (A) -> Maybe<A>, p: (A) -> Boolean) {
        identity(v, f).shouldBeTrue()
        filterAConsistency(v, p).shouldBeTrue()
    }
}

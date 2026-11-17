package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.matchers.booleans.shouldBeTrue

class MonoidLaw<T, M: Monoid<T, M>>(val clazz: Class<M>) {
    fun leftIdentity(a: M): Boolean =
            Monoid.op(clazz, a, Monoid.mzero(clazz)) == a

    fun rightIdentity(a: M): Boolean =
            Monoid.op(clazz, Monoid.mzero(clazz), a) == a

    fun associativity(a: M, b: M, c: M) =
            Monoid.op(clazz, Monoid.op(clazz, a, b), c) == Monoid.op(clazz, a, Monoid.op(clazz, b, c))

    fun assertSatisfyingMonoidLaw(a: M, b: M, c: M) {
        rightIdentity(a).shouldBeTrue()
        leftIdentity(a).shouldBeTrue()
        associativity(a, b, c).shouldBeTrue()
    }
}
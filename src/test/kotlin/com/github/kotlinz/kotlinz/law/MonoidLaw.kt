package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.Monoid
import com.winterbe.expekt.expect

class MonoidLaw<T, M: Monoid<T, M>>(val clazz: Class<M>) {
    fun leftIdentity(a: M): Boolean =
            Monoid.op(clazz, a, clazz.newInstance().mzero()) == a

    fun rightIdentity(a: M): Boolean =
            Monoid.op(clazz, clazz.newInstance().mzero(), a) == a

    fun associativity(a: M, b: M, c: M) =
            Monoid.op(clazz, Monoid.op(clazz, a, b), c) == Monoid.op(clazz, a, Monoid.op(clazz, b, c))

    fun assertSatisfyingMonoidLaw(a: M, b: M, c: M) {
        expect(rightIdentity(a)).to.be.`true`
        expect(leftIdentity(a)).to.be.`true`
        expect(associativity(a, b, c)).to.be.`true`
    }
}
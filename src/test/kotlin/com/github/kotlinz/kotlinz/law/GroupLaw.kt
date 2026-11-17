package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.Group
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.matchers.booleans.shouldBeTrue

class GroupLaw<T, G: Group<T, G>>(val clazz: Class<G>) {
    fun leftInverse(a: G): Boolean =
            Monoid.op(clazz, Group.inverse(clazz, a), a) == Monoid.mzero(clazz)

    fun rightInverse(a: G): Boolean =
            Monoid.op(clazz, a, Group.inverse(clazz, a)) == Monoid.mzero(clazz)

    fun assertSatisfyingGroupLaw(a: G, b: G, c: G) {
        MonoidLaw(clazz).assertSatisfyingMonoidLaw(a, b, c)
        leftInverse(a).shouldBeTrue()
        rightInverse(a).shouldBeTrue()
    }
}

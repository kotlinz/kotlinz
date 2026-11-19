package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.CommutativeMonoid
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.matchers.booleans.shouldBeTrue

class CommutativeMonoidLaw<T, M: CommutativeMonoid<T, M>>(val clazz: Class<M>) {
    fun commutativity(a: M, b: M) = Monoid.op(clazz, a, b) == Monoid.op(clazz, b, a)

    fun assertSatisfyingCommutativeMonoidLaw(a: M, b: M, c: M) {
        MonoidLaw(clazz).assertSatisfyingMonoidLaw(a, b, c)
        listOf(commutativity(a, b), commutativity(b, c), commutativity(a, c)).forEach { it.shouldBeTrue() }
    }
}

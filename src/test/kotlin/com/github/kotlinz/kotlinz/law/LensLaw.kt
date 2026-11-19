package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.optics.Lens
import com.github.kotlinz.kotlinz.optics.get
import com.github.kotlinz.kotlinz.optics.set
import io.kotest.matchers.booleans.shouldBeTrue

class LensLaw<S, A>(val lens: Lens<S, S, A, A>) {
    fun getSet(s: S, a: A): Boolean = lens.get(lens.set(a)(s)) == a

    fun setGet(s: S): Boolean = lens.set(lens.get(s))(s) == s

    fun setSet(s: S, a1: A, a2: A): Boolean = lens.set(a2)(lens.set(a1)(s)) == lens.set(a2)(s)

    fun assertSatisfyingLensLaw(ss: List<S>, aa: List<A>) {
        ss.all { s -> aa.all { a -> getSet(s, a) } }.shouldBeTrue()
        ss.all(::setGet).shouldBeTrue()
        ss.all { s -> aa.all { a1 -> aa.all { a2 -> setSet(s, a1, a2) } } }.shouldBeTrue()
    }
}

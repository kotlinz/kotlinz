package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.optics.Iso
import com.github.kotlinz.kotlinz.optics.get
import com.github.kotlinz.kotlinz.optics.reverseGet
import io.kotest.matchers.booleans.shouldBeTrue

class IsoLaw<S, A>(val iso: Iso<S, S, A, A>) {
    fun getThenReverseGet(s: S): Boolean = iso.reverseGet(iso.get(s)) == s

    fun reverseGetThenGet(a: A): Boolean = iso.get(iso.reverseGet(a)) == a

    fun assertSatisfyingIsoLaw(ss: List<S>, aa: List<A>) {
        ss.all(::getThenReverseGet).shouldBeTrue()
        aa.all(::reverseGetThenGet).shouldBeTrue()
    }
}

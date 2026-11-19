package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.optics.Prism
import com.github.kotlinz.kotlinz.optics.getMaybe
import com.github.kotlinz.kotlinz.optics.reverseGet
import io.kotest.matchers.booleans.shouldBeTrue

class PrismLaw<S, A>(val prism: Prism<S, S, A, A>) {
    fun reverseGetThenGetMaybe(a: A): Boolean = prism.getMaybe(prism.reverseGet(a)) == Maybe.Just(a)

    fun getMaybeThenReverseGet(s: S): Boolean = when (val focus = prism.getMaybe(s)) {
        is Maybe.Just -> prism.reverseGet(focus.value) == s
        is Maybe.None -> true
    }

    fun assertSatisfyingPrismLaw(ss: List<S>, aa: List<A>) {
        aa.all(::reverseGetThenGetMaybe).shouldBeTrue()
        ss.all(::getMaybeThenReverseGet).shouldBeTrue()
    }
}

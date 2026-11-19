package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.optics.Optional
import com.github.kotlinz.kotlinz.optics.getMaybe
import com.github.kotlinz.kotlinz.optics.set
import io.kotest.matchers.booleans.shouldBeTrue

class OptionalLaw<S, A>(val optional: Optional<S, S, A, A>) {
    fun getSet(s: S, a: A): Boolean = optional.getMaybe(optional.set(a)(s)) == optional.getMaybe(s).fmap { a }

    fun setGet(s: S): Boolean = when (val focus = optional.getMaybe(s)) {
        is Maybe.Just -> optional.set(focus.value)(s) == s
        is Maybe.None -> true
    }

    fun setSet(s: S, a1: A, a2: A): Boolean = optional.set(a2)(optional.set(a1)(s)) == optional.set(a2)(s)

    fun assertSatisfyingOptionalLaw(ss: List<S>, aa: List<A>) {
        ss.all { s -> aa.all { a -> getSet(s, a) } }.shouldBeTrue()
        ss.all(::setGet).shouldBeTrue()
        ss.all { s -> aa.all { a1 -> aa.all { a2 -> setSet(s, a1, a2) } } }.shouldBeTrue()
    }
}

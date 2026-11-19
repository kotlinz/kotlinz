package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.optics.Setter
import com.github.kotlinz.kotlinz.optics.over
import com.github.kotlinz.kotlinz.optics.set
import io.kotest.matchers.booleans.shouldBeTrue

class SetterLaw<S, A>(val setter: Setter<S, S, A, A>) {
    fun identity(s: S): Boolean = setter.over { a: A -> a }(s) == s

    fun composition(s: S, f: (A) -> A, g: (A) -> A): Boolean = setter.over(f)(setter.over(g)(s)) == setter.over { a: A -> f(g(a)) }(s)

    fun idempotence(s: S, a: A): Boolean = setter.set(a)(setter.set(a)(s)) == setter.set(a)(s)

    fun assertSatisfyingSetterLaw(ss: List<S>, aa: List<A>, f: (A) -> A, g: (A) -> A) {
        ss.all(::identity).shouldBeTrue()
        ss.all { composition(it, f, g) }.shouldBeTrue()
        ss.all { s -> aa.all { a -> idempotence(s, a) } }.shouldBeTrue()
    }
}
